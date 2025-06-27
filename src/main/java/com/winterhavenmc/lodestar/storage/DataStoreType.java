/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.lodestar.storage;

import com.winterhavenmc.lodestar.PluginMain;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;


public enum DataStoreType
{
	SQLITE("SQLite", "destinations.db")
			{
				@Override
				public DataStore connect(final PluginMain plugin)
				{

					// return new sqlite datastore object
					return new DataStoreSQLite(plugin);
				}

				@Override
				boolean storageObjectExists(final PluginMain plugin)
				{
					// get path name to data store file
					File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getStorageName());
					return dataStoreFile.exists();
				}
			};

	// DataStore display name
	private final String displayName;

	// datastore object name
	private final String storageName;

	// default datastore type
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Class constructor
	 *
	 * @param displayName the formatted display name of the datastore type
	 */
	DataStoreType(final String displayName, final String storageName)
	{
		this.displayName = displayName;
		this.storageName = storageName;
	}


	/**
	 * Create datastore
	 *
	 * @return DataStore object
	 */
	public abstract DataStore connect(final PluginMain plugin);


	@Override
	public String toString()
	{
		return displayName;
	}


	/**
	 * Getter for storage object name.
	 *
	 * @return the name of the backing store object for a data store type
	 */
	String getStorageName()
	{
		return storageName;
	}


	/**
	 * Test if datastore backing object (file, database) exists
	 *
	 * @param plugin reference to plugin main class
	 * @return true if backing object exists, false if not
	 */
	abstract boolean storageObjectExists(final PluginMain plugin);


	/**
	 * attempt get a datastore type by matching the name to a string; returns default type if no match
	 *
	 * @param displayName the string to match
	 * @return DataStoreType - the matched datastore type, or the default type if no match
	 */
	public static DataStoreType match(final String displayName)
	{
		for (DataStoreType type : DataStoreType.values())
		{
			if (type.toString().equalsIgnoreCase(displayName))
			{
				return type;
			}
		}
		// no match; return default type
		return defaultType;
	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to convert from
	 * @param newDataStore the new datastore to convert to
	 */
	static void convert(final PluginMain plugin, final DataStore oldDataStore, final DataStore newDataStore)
	{

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType()))
		{
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.getType().storageObjectExists(plugin))
		{
			plugin.getLogger().info("Converting existing " + oldDataStore + " datastore to "
					+ newDataStore + " datastore...");

			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized())
			{
				try
				{
					oldDataStore.initialize();
				}
				catch (Exception e)
				{
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			// get count of records inserted in new datastore from old datastore
			int count = newDataStore.insertRecords(oldDataStore.selectAllRecords());

			// log record count message
			plugin.getLogger().info(count + " records converted to " + newDataStore + " datastore.");

			// flush new datastore to disk if applicable
			newDataStore.sync();

			// close old datastore
			oldDataStore.close();

			// delete old datastore
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the new datastore to convert all other datastore into
	 */
	static void convertAll(final PluginMain plugin, final DataStore newDataStore)
	{
		// get array list of all data store types
		Collection<DataStoreType> dataStores = new HashSet<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore.getType());

		// convert each datastore type in list to new datastore type
		for (DataStoreType type : dataStores)
		{
			convert(plugin, type.connect(plugin), newDataStore);
		}
	}

}
