package com.winterhaven_mc.lodestar.storage;


abstract class DataStoreAbstract implements DataStore {

	private boolean initialized;

	DataStoreType type;

	String filename;


	/**
	 * Initialize storage
	 *
	 * @throws Exception on error
	 */
	public abstract void initialize() throws Exception;


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
	 * Get datastore filename or equivalent
	 *
	 * @return the filename or equivalent of the current datastore
	 */
	public String getFilename() {
		return this.filename;
	}


	/**
	 * Get datastore type
	 */
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Get datastore name
	 *
	 * @return the formatted display name of the current datastore
	 */
	@Override
	public String getName() {
		return this.getType().toString();
	}


	/**
	 * Get datastore initialized field
	 *
	 * @return boolean
	 */
	@Override
	public boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set initialized field
	 *
	 * @param initialized the boolean value to set initialized field
	 */
	@Override
	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

}
