package com.winterhaven_mc.lodestar;

public enum DataStoreType {

	YAML("yaml") {
		
		@Override
		public DataStore create() {
			
			// create new sqlite datastore object
			return new DataStoreYAML(plugin);
		}
	},
	
	SQLITE("SQLite") {
		
		@Override
		public DataStore create() {
			
			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	private String displayName;

	private final static LodeStarMain plugin = LodeStarMain.instance;
	
	private final static DataStoreType defaultType = DataStoreType.SQLITE;
	
	public abstract DataStore create();
	
	/**
	 * Class constructor
	 * @param displayName
	 */
	private DataStoreType(String displayName) {
		this.setDisplayName(displayName);
	}
	
	public String getName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public static DataStoreType match(String displayName) {
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
