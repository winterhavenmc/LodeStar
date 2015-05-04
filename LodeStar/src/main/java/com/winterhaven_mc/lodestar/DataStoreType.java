package com.winterhaven_mc.lodestar;

public enum DataStoreType {

	YAML("yaml",null),
	SQLITE("sqlite",YAML);

	private String name;
	private DataStoreType fallback;

	private DataStoreType(String name, DataStoreType fallback) {
		this.setName(name);
		this.setFallback(fallback);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public DataStoreType getFallback() {
		return fallback;
	}
	
	public void setFallback(DataStoreType fallback) {
		this.fallback = fallback;
	}
	
	public static DataStoreType match(String name) {
		for (DataStoreType type : DataStoreType.values()) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}
