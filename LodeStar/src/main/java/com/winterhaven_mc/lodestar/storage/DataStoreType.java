package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;


enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create() {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};


	private String displayName;

	// static reference to main class
	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	// default datastore type
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Create datastore
	 * @return DataStore object
	 */
	public abstract DataStore create();


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

}
