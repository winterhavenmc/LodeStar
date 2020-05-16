package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


class DataStoreSQLite extends DataStore {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	DataStoreSQLite(final PluginMain plugin) {

		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set datastore filename
		this.filename = "destinations.db";
	}


	@Override
	void initialize() throws SQLException, ClassNotFoundException {

		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			plugin.getLogger().info(this.getName() + " datastore already initialized.");
			return;
		}

		// sql statement to create table if it doesn't already exist
		final String createDestinationTable = "CREATE TABLE IF NOT EXISTS destinations ("
				+ "key VARCHAR PRIMARY KEY, "
				+ "displayname VARCHAR,"
				+ "worldname VARCHAR(255) NOT NULL, "
				+ "x DOUBLE, "
				+ "y DOUBLE, "
				+ "z DOUBLE, "
				+ "yaw FLOAT, "
				+ "pitch FLOAT) ";

		// register the driver 
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String destinationsDb = plugin.getDataFolder() + File.separator + filename;
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + destinationsDb;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);
		Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(createDestinationTable);

		// set initialized true
		setInitialized(true);
		if (plugin.debug) {
			plugin.getLogger().info(this.getName() + " datastore initialized.");
		}

	}


	@Override
	public Destination getRecord(final String key) {

		String derivedKey = key;

		// if key is null return null record
		if (derivedKey == null) {
			return null;
		}

		// derive key in case destination name was passed
		derivedKey = Destination.deriveKey(derivedKey);

		Destination destination = null;
		World world;

		final String sqlGetDestination = "SELECT * FROM destinations WHERE key = ?";

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sqlGetDestination);

			preparedStatement.setString(1, derivedKey);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (rs.next()) {

				// get stored displayName
				String displayName = rs.getString("displayname");
				if (displayName == null || displayName.isEmpty()) {
					displayName = derivedKey;
				}

				// get stored world and coordinates
				String worldName = rs.getString("worldname");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");
				float yaw = rs.getFloat("yaw");
				float pitch = rs.getFloat("pitch");

				if (plugin.getServer().getWorld(worldName) == null) {
					plugin.getLogger().warning("Stored destination world not found!");
					return null;
				}
				world = plugin.getServer().getWorld(worldName);
				Location location = new Location(world, x, y, z, yaw, pitch);
				destination = new Destination(derivedKey, displayName, location);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while fetching a destination from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
			return null;
		}
		return destination;
	}


	@Override
	public void putRecord(final Destination destination) {

		// if destination is null do nothing and return
		if (destination == null) {
			return;
		}

		// get key
		final String key = destination.getKey();

		// get display name
		final String displayName = destination.getDisplayName();

		// get location
		final Location location = destination.getLocation();

		// get world name
		String testWorldName;

		// test that world in destination location is valid
		try {
			testWorldName = Objects.requireNonNull(location.getWorld()).getName();
		}
		catch (Exception e) {
			plugin.getLogger().warning("An error occured while inserting"
					+ " a destination in the " + toString() + " datastore. World invalid!");
			return;
		}
		final String worldName = testWorldName;

		// sql statement to insert or replace record
		final String sqlInsertDestination = "INSERT OR REPLACE INTO destinations ("
				+ "key, "
				+ "displayname,"
				+ "worldname, "
				+ "x, "
				+ "y, "
				+ "z, "
				+ "yaw, "
				+ "pitch) "
				+ "values(?,?,?,?,?,?,?,?)";

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertDestination);

					preparedStatement.setString(1, key);
					preparedStatement.setString(2, displayName);
					preparedStatement.setString(3, worldName);
					preparedStatement.setDouble(4, location.getX());
					preparedStatement.setDouble(5, location.getY());
					preparedStatement.setDouble(6, location.getZ());
					preparedStatement.setFloat(7, location.getYaw());
					preparedStatement.setFloat(8, location.getPitch());

					// execute prepared statement
					preparedStatement.executeUpdate();
				}
				catch (Exception e) {

					// output simple error message
					plugin.getLogger().warning("An error occured while inserting a destination "
							+ "into the " + toString() + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	@Override
	public List<String> getAllKeys() {

		List<String> returnList = new ArrayList<>();

		// sql statement to retrieve all display names
		final String sqlSelectAllKeys = "SELECT key FROM destinations ORDER BY key";

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAllKeys);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				returnList.add(rs.getString("key"));
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return results
		return returnList;
	}


	@Override
	List<Destination> getAllRecords() {

		List<Destination> returnList = new ArrayList<>();

		// sql statement to retrieve all display names
		final String sqlSelectAllRecords = "SELECT * FROM destinations ORDER BY key";

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAllRecords);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				String key = rs.getString("key");
				String displayName = rs.getString("displayname");
				String worldName = rs.getString("worldname");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");
				float yaw = rs.getFloat("yaw");
				float pitch = rs.getFloat("pitch");

				World world;

				try {
					world = plugin.getServer().getWorld(worldName);
				}
				catch (Exception e) {
					plugin.getLogger().warning("Stored destination has unloaded world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				Location location = new Location(world, x, y, z, yaw, pitch);
				Destination destination = new Destination(key, displayName, location);
				returnList.add(destination);
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all records from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return results
		return returnList;

	}


	@Override
	public Destination deleteRecord(String key) {

		// if key is null return null record
		if (key == null) {
			return null;
		}

		// derive key in case destination name was passed
		key = Destination.deriveKey(key);

		// get destination record to be deleted, for return
		Destination destination = this.getRecord(key);

		final String sqlDeleteDestination = "DELETE FROM destinations WHERE key = ?";

		try {
			// create prepared statement
			PreparedStatement preparedStatement = connection.prepareStatement(sqlDeleteDestination);

			preparedStatement.setString(1, key);

			// execute prepared statement
			int rowsAffected = preparedStatement.executeUpdate();

			// output debugging information
			if (plugin.debug) {
				plugin.getLogger().info(rowsAffected + " rows deleted.");
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to "
					+ "delete a destination from the SQLite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}
		return destination;
	}


	@Override
	public void close() {

		try {
			connection.close();
			plugin.getLogger().info("SQLite datastore connection closed.");
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while closing the SQLite datastore.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}
		setInitialized(false);
	}


	@Override
	void sync() {

		// no action necessary for this storage type

	}


	@Override
	boolean delete() {

		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		boolean result = false;
		if (dataStoreFile.exists()) {
			result = dataStoreFile.delete();
		}
		return result;
	}


	@Override
	boolean exists() {

		// get path name to data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();
	}

}
