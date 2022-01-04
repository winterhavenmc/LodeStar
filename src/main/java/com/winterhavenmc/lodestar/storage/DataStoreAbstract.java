package com.winterhavenmc.lodestar.storage;


abstract class DataStoreAbstract implements DataStore {

	private boolean initialized;

	DataStoreType type;


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
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Get datastore type
	 */
	@Override
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Override toString method to return the datastore type name
	 *
	 * @return the name of this datastore instance
	 */
	@Override
	public String toString() {
		return this.type.toString();
	}

}
