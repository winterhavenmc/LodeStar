package com.winterhaven_mc.lodestar.storage;

import java.util.List;


public abstract class DataStore {

	private boolean initialized;
	
	DataStoreType type;

	String filename;

	/**
	 * Initialize storage
	 * @throws Exception on error
	 */
	abstract void initialize() throws Exception;
	
	/**
	 * Get record
	 * @param destinationName the name string key of the destination to be retrieved from the datastore
	 * @return destination object or null if no matching record
	 */
	public abstract Destination getRecord(final String destinationName);
	
	/**
	 * Store record
	 * @param destination the destination object to be inserted in the datastore
	 */
	public abstract void putRecord(final Destination destination);

	/**
	 * get all display names
	 * @return List of all destination display name strings
	 */
	public abstract List<String> getAllKeys();
	
	/**
	 * get all records
	 * @return List of all destination records
	 */
	abstract List<Destination> getAllRecords();

	/**
	 * Delete record
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
	 * @return boolean
	 */
	abstract boolean exists();
	
	/**
	 * Get datastore filename or equivalent
	 * @return the filename or equivalent of the current datastore
	 */
	String getFilename() {
		return this.filename;
	}

	/**
	 * Get datastore type
	 */
	DataStoreType getType() {
		return this.type;
	}
	
	/**
	 * Get datastore name
	 * @return the formatted display name of the current datastore
	 */
	public String getName() {
		return this.getType().toString();
	}

	/**
	 * Get datastore initialized field
	 * @return boolean
	 */
	boolean isInitialized() {
		return this.initialized;
	}
	
	/**
	 * Set initialized field
	 * @param initialized the boolean value to set initialized field
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}
	
}
