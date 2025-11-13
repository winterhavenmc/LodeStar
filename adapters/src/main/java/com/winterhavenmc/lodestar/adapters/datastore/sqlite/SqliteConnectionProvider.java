/*
 * Copyright (c) 2025 Tim Savage.
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

package com.winterhavenmc.lodestar.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.lodestar.models.destination.*;
import com.winterhavenmc.lodestar.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.plugin.ports.datastore.DestinationRepository;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import static com.winterhavenmc.lodestar.adapters.datastore.sqlite.SqliteMessage.datastoreName;


public class SqliteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final Server server;
	private final Logger logger;
	private final String dataFilePath;
	private final ConfigRepository configRepository;
	private Connection connection;
	private boolean initialized;
	private DestinationRepository destinationRepository;


	/**
	 * Class constructor
	 *
	 */
	public SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.server = plugin.getServer();
		this.dataFilePath = plugin.getDataFolder() + File.separator + "destinations.db";
		this.configRepository = BukkitConfigRepository.create(plugin);

		// initialize data store
		try
		{
			this.initialize();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}
	}


	public ConnectionProvider connect()
	{
		// initialize data store
		try
		{
			this.initialize();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}

		// return initialized data store
		return this;
	}


	/**
	 * Initialize datastore
	 */
	private void initialize() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, do nothing and return
		if (initialized)
		{
			logger.info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(configRepository.locale(), datastoreName));
			return;
		}

		// register the driver
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// update database schema if necessary
		updateSchema();

		// set initialized true
		initialized = true;

		// instantiate datastore adapters
		this.destinationRepository = new SqliteDestinationRepository(plugin, connection, configRepository);

		// output log message
		logger.info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(configRepository.locale(), datastoreName));
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close()
	{
		try
		{
			connection.close();
			logger.info(SqliteMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(configRepository.locale(), datastoreName));
		}
		catch (Exception e)
		{
			// output simple error message
			logger.warning(SqliteMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(configRepository.locale(), datastoreName));
			logger.warning(e.getMessage());
		}

		initialized = false;
	}


	@Override
	public DestinationRepository destinations()
	{
		return this.destinationRepository;
	}


	private void updateSchema() throws SQLException
	{
		int schemaVersion = getSchemaVersion();

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0)
		{
			int count;
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("SelectDestinationTable"));
			if (resultSet.next())
			{
				Collection<StoredDestination> existingRecords = getAll();
				statement.executeUpdate(SqliteQueries.getQuery("DropDestinationTable"));
				statement.executeUpdate(SqliteQueries.getQuery("CreateDestinationTable"));
				count = destinationRepository.save(existingRecords);
				logger.info(count + " destination records migrated to schema v1");
			}

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = 1");
		}

		// execute table creation statement
		statement.executeUpdate(SqliteQueries.getQuery("CreateDestinationTable"));
	}


	private Collection<StoredDestination> getAll()
	{
		return (getSchemaVersion() == 0)
				? getAll_V0()
				: getAll_V1();
	}


	private Collection<StoredDestination> getAll_V0()
	{
		Collection<StoredDestination> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllRecords")))
		{
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				String displayName = resultSet.getString("displayname");
				String worldName = resultSet.getString("worldname");
				double x = resultSet.getDouble("x");
				double y = resultSet.getDouble("y");
				double z = resultSet.getDouble("z");
				float yaw = resultSet.getFloat("yaw");
				float pitch = resultSet.getFloat("pitch");

				// get world by name
				World world = server.getWorld(worldName);

				UUID worldUid = null;

				// if world is null, set worldValid false and log warning
				if (world == null)
				{
					logger.warning("Stored validDestination has invalid world: " + worldName);
				}
				else
				{
					worldUid = world.getUID();
				}

				Destination destination = StoredDestination.of(displayName, worldName, worldUid, x, y, z, yaw, pitch);

				if (destination instanceof StoredDestination storedDestination)
				{
					returnList.add(storedDestination);
				}
			}
		}
		catch (final SQLException e)
		{
			logger.warning(SqliteMessage.SELECT_ALL_RECORDS_ERROR.getLocalizedMessage(configRepository.locale(), datastoreName));
			logger.warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	private Collection<StoredDestination> getAll_V1()
	{
		Collection<StoredDestination> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllRecords")))
		{
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				String displayName = resultSet.getString("displayname");
				String worldName = resultSet.getString("worldname");
				long worldUidMsb = resultSet.getLong("worldUidMsb");
				long worldUidLsb = resultSet.getLong("worldUidLsb");
				double x = resultSet.getDouble("x");
				double y = resultSet.getDouble("y");
				double z = resultSet.getDouble("z");
				float yaw = resultSet.getFloat("yaw");
				float pitch = resultSet.getFloat("pitch");

				// reconstitute world uid from components
				UUID worldUid = new UUID(worldUidMsb, worldUidLsb);

				// get world
				World world = server.getWorld(worldUid);

				// if world is null, set worldValid false and log warning
				if (world == null)
				{
					logger.warning("Stored destination has invalid world: " + worldName);
				}

				Destination destination = StoredDestination.of(displayName, worldName, worldUid, x, y, z, yaw, pitch);

				if (destination instanceof StoredDestination storedDestination)
				{
					returnList.add(storedDestination);
				}
			}
		}
		catch (final SQLException e)
		{
			logger.warning(SqliteMessage.SELECT_ALL_RECORDS_ERROR.getLocalizedMessage(configRepository.locale(), datastoreName));
			logger.warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	int getSchemaVersion()
	{
		int version = 0;

		try (Statement statement = connection.createStatement())
		{
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("GetUserVersion"));
			if (resultSet.next())
			{
				version = resultSet.getInt(1);
			}
		}
		catch (SQLException sqlException)
		{
			logger.warning("Could not get schema version!");
		}

		return version;
	}

}
