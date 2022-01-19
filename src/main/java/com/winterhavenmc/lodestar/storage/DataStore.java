package com.winterhavenmc.lodestar.storage;

import com.winterhavenmc.lodestar.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;


public interface DataStore {

	/**
	 * Initialize storage
	 *
	 * @throws Exception on error
	 */
	void initialize() throws Exception;


	/**
	 * Get datastore initialized field
	 *
	 * @return boolean
	 */
	boolean isInitialized();


	/**
	 * Get datastore type
	 */
	DataStoreType getType();


	/**
	 * Get record
	 *
	 * @param destinationName the name string key of the destination to be retrieved from the datastore
	 * @return destination object or null if no matching record
	 */
	Destination selectRecord(final String destinationName);


	/**
	 * Store record
	 *
	 * @param destination the destination object to be inserted in the datastore
	 */
	void insertRecord(final Destination destination);


	/**
	 * Insert a collection of records
	 * @param destinations a collection of records to be inserted
	 *
	 * @return count of records inserted
	 */
	int insertRecords(final Collection<Destination> destinations);


	/**
	 * get all display names
	 *
	 * @return List of all destination display name strings
	 */
	List<String> selectAllKeys();


	/**
	 * get all records
	 *
	 * @return List of all destination records
	 */
	Collection<Destination> selectAllRecords();


	/**
	 * Delete record
	 *
	 * @param destinationName the name key string of the destination record to be deleted
	 * @return the destination record that was deleted
	 */
	@SuppressWarnings("UnusedReturnValue")
	Destination deleteRecord(final String destinationName);


	/**
	 * Close storage
	 */
	void close();


	/**
	 * Sync datastore to disk if supported
	 */
	void sync();


	/**
	 * Delete datastore
	 */
	@SuppressWarnings("UnusedReturnValue")
	boolean delete();


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	static DataStore connect(final JavaPlugin plugin) {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.connect(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize the " + newDataStore + " datastore!");
			plugin.getLogger().severe(e.getLocalizedMessage());
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// convert any existing data stores to new type
		DataStoreType.convertAll(plugin, newDataStore);

		// return initialized data store
		return newDataStore;
	}


	/**
	 * Reload datastore
	 */
	static void reload(final PluginMain plugin) {

		// get current datastore type
		final DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		final DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = connect(plugin);
		}
	}

}
