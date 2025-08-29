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

package com.winterhavenmc.lodestar.plugin.storage;

import com.winterhavenmc.lodestar.bootstrap.Bootstrap;
import com.winterhavenmc.lodestar.plugin.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.plugin.datastore.DestinationRepository;
import org.bukkit.plugin.Plugin;


public final class DataStore
{
	private final ConnectionProvider connectionProvider;


	/**
	 * Private constructor
	 */
	private DataStore(final ConnectionProvider connectionProvider)
	{
		this.connectionProvider = connectionProvider;
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param plugin reference to plugin main class
	 * @return a new datastore instance of the given type
	 */
	public static DataStore connect(final Plugin plugin)
	{
		ConnectionProvider connectionProvider = Bootstrap.getConnectionProvider(plugin);

		// initialize data store
		try
		{
			connectionProvider.connect();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
			// plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		// return initialized data store
		return new DataStore(connectionProvider);
	}


	/**
	 * Close datastore connection
	 */
	public void close()
	{
		connectionProvider.close();
	}


	public DestinationRepository destinations()
	{
		return connectionProvider.destinations();
	}

}
