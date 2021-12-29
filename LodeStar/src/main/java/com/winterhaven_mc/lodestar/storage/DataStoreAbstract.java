package com.winterhaven_mc.lodestar.storage;


abstract class DataStoreAbstract implements DataStore {

	private boolean initialized;

	DataStoreType type;

	String filename;


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
