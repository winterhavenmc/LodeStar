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

import com.winterhavenmc.library.messagebuilder.models.DefaultSymbol;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.InvalidDestination;
import com.winterhavenmc.lodestar.models.destination.StoredDestination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.ports.datastore.DestinationRepository;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.winterhavenmc.lodestar.adapters.datastore.sqlite.SqliteMessage.datastoreName;


final class SqliteDestinationRepository implements DestinationRepository
{
	private final Logger logger;
	private final Server server;
	private final FileConfiguration config;
	private final Connection connection;
	private final ConfigRepository configRepository;
	private final SqliteDestinationQueryExecutor queryExecutor = new SqliteDestinationQueryExecutor();


	/**
	 * Class constructor
	 *
	 */
	SqliteDestinationRepository(final Plugin plugin, final Connection connection, final ConfigRepository configRepository)
	{
		this.logger = plugin.getLogger();
		this.server = plugin.getServer();
		this.config = plugin.getConfig();
		this.connection = connection;
		this.configRepository = configRepository;
	}


	@Override
	public int save(final Collection<StoredDestination> storedDestinations)
	{
		// if destinations is null return zero record count
		if (storedDestinations == null)
		{
			return 0;
		}

		int count = 0;

		for (StoredDestination storedDestination : storedDestinations)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertDestination")))
			{
				count += queryExecutor.insertRecords(storedDestination, preparedStatement);
			}
			catch (SQLException sqlException)
			{
				logger.warning(SqliteMessage.INSERT_RECORD_ERROR.getLocalizedMessage(configRepository.logLocale(), datastoreName));
				logger.warning(sqlException.getLocalizedMessage());
			}
		}
		return count;
	}


	@Override
	public Destination get(final String key)
	{
		if (key == null) return new InvalidDestination("UNKNOWN", "Key was null");

		// derive key in case destination name was passed
		String derivedKey = deriveKey(key);

		Destination destination = null;

		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectDestination")))
		{
			preparedStatement.setString(1, derivedKey);

			// execute sql query
			ResultSet resultSet = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (resultSet.next())
			{
				// get stored displayName
				String displayName = resultSet.getString("displayname");
				if (displayName == null || displayName.isEmpty())
				{
					displayName = derivedKey;
				}

				// get stored world and coordinates
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
					logger.warning("Stored validDestination has invalid world: " + worldName);
				}

				// create Destination object
				destination = StoredDestination.of(displayName, worldName, worldUid, x, y, z, yaw, pitch);
			}
		}
		catch (SQLException sqlException)
		{
			// output simple error message
			logger.warning(SqliteMessage.SELECT_RECORD_ERROR.getDefaultMessage());
			logger.warning(sqlException.getLocalizedMessage());
			return new InvalidDestination(key, "Could not retrieve destination for key.");
		}

		return switch (destination)
		{
			case ValidDestination validDestination -> validDestination;
			case InvalidDestination invalidDestination -> invalidDestination;
			case null -> new InvalidDestination(key, "Could not retrieve destination for key");
		};
	}


	@Override
	public List<String> names()
	{
		List<String> returnList = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllKeys")))
		{
			ResultSet resultSet = queryExecutor.selectKeys(preparedStatement);

			while (resultSet.next())
			{
				returnList.add(resultSet.getString("key"));
			}
		}
		catch (SQLException sqlException)
		{
			// output simple error message
			logger.warning(SqliteMessage.SELECT_ALL_KEYS_ERROR.getLocalizedMessage(configRepository.logLocale()));
			logger.warning(sqlException.getLocalizedMessage());
		}

		return returnList;
	}


	@Override
	public Destination delete(final String passedKey)
	{
		// if key is null return invalid destination
		if (passedKey == null) { return new InvalidDestination(DefaultSymbol.NULL.symbol(), "Key was null."); }
		else
		{
			// derive key in case destination name was passed
			String key = deriveKey(passedKey);

			// get destination record to be deleted, for return
			Destination deletedDestination = this.get(key);

			try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("DeleteDestination")))
			{
				int rowsAffected = queryExecutor.deleteRecords(key, preparedStatement);

				// output debugging information
				if (config.getBoolean("debug"))
				{
					logger.info(rowsAffected + " rows deleted.");
				}
			}
			catch (Exception e)
			{
				// output simple error message
				logger.warning(SqliteMessage.DELETE_RECORD_ERROR.getLocalizedMessage(configRepository.logLocale(), datastoreName));
				logger.warning(e.getLocalizedMessage());
			}
			return deletedDestination;
		}
	}


	/**
	 * Derive key from destination display name<br>
	 * strips color codes and replaces spaces with underscores<br>
	 * if a destination key is passed, it will be returned unaltered
	 *
	 * @param destinationName the destination name to convert to a key
	 * @return String - the key derived from the destination name
	 */
	public String deriveKey(final String destinationName)
	{
		// validate parameter
		if (destinationName == null || destinationName.isBlank())
		{
			return "";
		}

		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', destinationName))
				.replace(' ', '_');
	}

}
