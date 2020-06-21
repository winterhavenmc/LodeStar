package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class DataStore {

	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private boolean initialized;

	DataStoreType type;

	String filename;


	/**
	 * Initialize storage
	 *
	 * @throws Exception on error
	 */
	abstract void initialize() throws Exception;


	/**
	 * Get record
	 *
	 * @param destinationName the name string key of the destination to be retrieved from the datastore
	 * @return destination object or null if no matching record
	 */
	public abstract Destination selectRecord(final String destinationName);


	/**
	 * Store record
	 *
	 * @param destination the destination object to be inserted in the datastore
	 */
	public abstract void insertRecord(final Destination destination);


	/**
	 * get all display names
	 *
	 * @return List of all destination display name strings
	 */
	public abstract List<String> selectAllKeys();


	/**
	 * get all records
	 *
	 * @return List of all destination records
	 */
	abstract List<Destination> selectAllRecords();


	/**
	 * Delete record
	 *
	 * @param destinationName the name key string of the destination record to be deleted
	 * @return the destination record that was deleted
	 */
	@SuppressWarnings("UnusedReturnValue")
	public abstract Destination deleteRecord(final String destinationName);


	/**
	 * Close storage
	 */
	public abstract void close();


	/**
	 * Sync datastore to disk if supported
	 */
	abstract void sync();


	/**
	 * Delete datastore
	 */
	@SuppressWarnings("UnusedReturnValue")
	abstract boolean delete();


	/**
	 * Check that datastore exists
	 *
	 * @return boolean
	 */
	abstract boolean exists();


	/**
	 * Get datastore filename or equivalent
	 *
	 * @return the filename or equivalent of the current datastore
	 */
	String getFilename() {
		return this.filename;
	}


	/**
	 * Get datastore type
	 */
	private DataStoreType getType() {
		return this.type;
	}


	/**
	 * Get datastore name
	 *
	 * @return the formatted display name of the current datastore
	 */
	public String getName() {
		return this.getType().toString();
	}


	/**
	 * Get datastore initialized field
	 *
	 * @return boolean
	 */
	boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set initialized field
	 *
	 * @param initialized the boolean value to set initialized field
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	public static DataStore create() {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param dataStoreType new datastore type
	 * @param oldDataStore  existing datastore reference
	 * @return the initialized new datastore
	 */
	private static DataStore create(final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create();

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore.toString() + " datastore!");
			if (plugin.debug) {
				e.printStackTrace();
			}
		}

		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convertDataStore(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to convert from
	 * @param newDataStore the new datastore to convert to
	 */
	private static void convertDataStore(final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {

			plugin.getLogger().info("Converting existing " + oldDataStore.toString() + " datastore to "
					+ newDataStore.toString() + " datastore...");

			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore.getName() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			// get List of all records from old datastore
			List<Destination> allRecords = oldDataStore.selectAllRecords();

			// initialize counter
			int count = 0;

			// insert each record into new datastore
			for (Destination record : allRecords) {
				newDataStore.insertRecord(record);
				count++;
			}
			plugin.getLogger().info(count + " records converted to " + newDataStore.getName() + " datastore.");

			newDataStore.sync();

			oldDataStore.close();
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the new datastore to convert all other datastore into
	 */
	private static void convertAll(final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore.getType());

		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;

			if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}

			// add additional datastore types here as they become available

			if (oldDataStore != null) {
				convertDataStore(oldDataStore, newDataStore);
			}
		}
	}


	/**
	 * Reload datastore
	 */
	public static void reload() {

		// get current datastore type
		final DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		final DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = create(newType, plugin.dataStore);
		}
	}

}
