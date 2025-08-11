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

import com.winterhavenmc.lodestar.destination.Destination;
import com.winterhavenmc.lodestar.destination.ValidDestination;
import com.winterhavenmc.lodestar.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.ports.datastore.DestinationRepository;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;


public class SqliteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final Logger logger;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;
	private DestinationRepository destinationRepository;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.dataFilePath = plugin.getDataFolder() + File.separator + "destinations.db";
	}


	/**
	 * Initialize datastore
	 */
	@Override
	public void connect() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, do nothing and return
		if (initialized)
		{
			logger.info("SQLite datastore already initialized.");
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
		this.destinationRepository = new SqliteDestinationRepository(plugin, connection);

		// output log message
		logger.info("Datastore initialized.");
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
			logger.info("SQLite datastore connection closed.");
		}
		catch (Exception e)
		{
			// output simple error message
			logger.warning("An error occurred while closing the SQLite datastore.");
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
				Collection<ValidDestination> existingRecords = getAll();
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


	private Collection<ValidDestination> getAll()
	{
		return (getSchemaVersion() == 0)
				? getAll_V0()
				: getAll_V1();
	}


	private Collection<ValidDestination> getAll_V0()
	{
		Collection<ValidDestination> returnList = new ArrayList<>();

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
				World world = plugin.getServer().getWorld(worldName);

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

				Destination destination = Destination.of(Destination.Type.STORED, displayName, worldName, worldUid, x, y, z, yaw, pitch);

				if (destination instanceof ValidDestination validDestination)
				{
					returnList.add(validDestination);
				}
			}
		}
		catch (final SQLException e)
		{
			logger.warning("An error occurred while trying to select all records from the SQLite datastore.");
			logger.warning(e.getLocalizedMessage());
		}

		return returnList;
	}


	private Collection<ValidDestination> getAll_V1()
	{
		Collection<ValidDestination> returnList = new ArrayList<>();

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
				World world = plugin.getServer().getWorld(worldUid);

				// if world is null, set worldValid false and log warning
				if (world == null)
				{
					logger.warning("Stored validDestination has invalid world: " + worldName);
				}

				Destination destination = Destination.of(Destination.Type.STORED, displayName, worldName, worldUid, x, y, z, yaw, pitch);

				if (destination instanceof ValidDestination validDestination)
				{
					returnList.add(validDestination);
				}
			}
		}
		catch (final SQLException e)
		{
			logger.warning("An error occurred while trying to select all records from the SQLite datastore.");
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
			plugin.getLogger().warning("Could not get schema version!");
		}

		return version;
	}

}
