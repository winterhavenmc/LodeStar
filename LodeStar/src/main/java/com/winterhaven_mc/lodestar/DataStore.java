package com.winterhaven_mc.lodestar;

import java.util.List;

public abstract class DataStore {

	protected boolean initialized;
	
	/**
	 * Initialize storage
	 * @throws Exception
	 */
	abstract void initialize() throws Exception;
	
	/**
	 * Get record
	 * @param warpName
	 * @return warp object or null if no matching record
	 */
	abstract Destination getRecord(String warpName);
	
	/**
	 * Store record
	 * @param destination
	 */
	abstract void putRecord(Destination destination);

	/**
	 * get all display names
	 * @return
	 */
	abstract List<String> getAllKeys();
	
	/**
	 * get all records
	 * @return
	 */
	abstract List<Destination> getAllRecords();

	/**
	 * Delete record
	 * @param warpName
	 * @return 
	 */	
	abstract Destination deleteRecord(String warpName);
	
	/**
	 * Close storage
	 */
	abstract void close();

	abstract void save();
	
	abstract void delete();
	
	abstract boolean exists();
	
	abstract String getFilename();
	
	abstract DataStoreType getType();
	
	String getName() {
		return this.getType().getName();
	}
	
	boolean isInitialized() {
		return this.initialized;
	}
	
	void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
}
