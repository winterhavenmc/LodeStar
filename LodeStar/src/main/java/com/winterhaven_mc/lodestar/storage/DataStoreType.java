package com.winterhaven_mc.lodestar.storage;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create(final JavaPlugin plugin) {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	// DataStore display name
	private String displayName;

	// default datastore type
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Create datastore
	 * @return DataStore object
	 */
	public abstract DataStore create(final JavaPlugin plugin);


	/**
	 * Class constructor
	 *
	 * @param displayName the formatted display name of the datastore type
	 */
	DataStoreType(final String displayName) {
		this.setDisplayName(displayName);
	}


	public String getName() {
		return displayName;
	}


	@Override
	public String toString() {
		return displayName;
	}


	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}


	public static DataStoreType match(final String displayName) {
		for (DataStoreType type : DataStoreType.values()) {
			if (type.toString().equalsIgnoreCase(displayName)) {
				return type;
			}
		}
		// no match; return default type
		return defaultType;
	}


	public static DataStoreType getDefaultType() {
		return defaultType;
	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to convert from
	 * @param newDataStore the new datastore to convert to
	 */
	static void convertDataStore(final JavaPlugin plugin, final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {

			plugin.getLogger().info("Converting existing " + oldDataStore + " datastore to "
					+ newDataStore + " datastore...");

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
	static void convertAll(final JavaPlugin plugin, final DataStore newDataStore) {

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
				convertDataStore(plugin, oldDataStore, newDataStore);
			}
		}
	}

}
