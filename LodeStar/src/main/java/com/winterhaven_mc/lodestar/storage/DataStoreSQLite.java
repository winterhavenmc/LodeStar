package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;


class DataStoreSQLite extends DataStore {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;

	// schema version
	private int schemaVersion;

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

		// register the driver
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String destinationsDb = plugin.getDataFolder() + File.separator + filename;
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + destinationsDb;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// update schema if necessary
		updateSchema();

		// set initialized true
		setInitialized(true);
		plugin.getLogger().info(this.getName() + " datastore initialized.");
	}


	private int getSchemaVersion() {

		int version = -1;

		try {
			final Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			while (rs.next()) {
				version = rs.getInt(1);
			}
		}
		catch (SQLException e) {
			plugin.getLogger().warning("Could not get schema version!");
		}
		return version;
	}


	private void updateSchema() throws SQLException {

		schemaVersion = getSchemaVersion();

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0) {
			int count;
			ResultSet rs = statement.executeQuery(Queries.getQuery("SelectDestinationTable"));
			if (rs.next()) {
				Collection<Destination> existingRecords = selectAllRecords();
				statement.executeUpdate(Queries.getQuery("DropDestinationTable"));
				statement.executeUpdate(Queries.getQuery("CreateDestinationTable"));
				count = insertRecords(existingRecords);
				plugin.getLogger().info(count + " destination records migrated to schema v1");
			}

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = 1");

			// update schema version field
			schemaVersion = 1;
		}

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateDestinationTable"));
	}


	@Override
	public void insertRecord(final Destination destination) {

		// if destination is null do nothing and return
		if (destination == null) {
			return;
		}

		// get key
		final String key = destination.getKey();

		// get display name
		final String displayName = destination.getDisplayName();

		// get world
		World world = plugin.getServer().getWorld(destination.getWorldUid());

		// test that world in destination location is valid
		if (world == null) {
			plugin.getLogger().warning("An error occured while inserting"
					+ " a destination in the " + toString() + " datastore. World invalid!");
			return;
		}

		// get current name of world
		final String worldName = world.getName();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDestination"));

					preparedStatement.setString(1, key);
					preparedStatement.setString(2, displayName);
					preparedStatement.setString(3, worldName);
					preparedStatement.setLong(4, destination.getWorldUid().getMostSignificantBits());
					preparedStatement.setLong(5, destination.getWorldUid().getLeastSignificantBits());
					preparedStatement.setDouble(6, destination.getX());
					preparedStatement.setDouble(7, destination.getY());
					preparedStatement.setDouble(8, destination.getZ());
					preparedStatement.setFloat(9, destination.getYaw());
					preparedStatement.setFloat(10, destination.getPitch());

					// execute prepared statement
					preparedStatement.executeUpdate();
				}
				catch (SQLException e) {

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
	public synchronized int insertRecords(final Collection<Destination> destinations) {

		// if destination is null return zero record count
		if (destinations == null) {
			return 0;
		}

		int count = 0;

		for (Destination destination : destinations) {

			// get key
			final String key = destination.getKey();

			// get display name
			final String displayName = destination.getDisplayName();

			// get world
			World world = plugin.getServer().getWorld(destination.getWorldUid());

			// test that world in destination location is valid
			if (world == null) {
				plugin.getLogger().warning("An error occured while inserting"
						+ " a destination in the " + toString() + " datastore. World invalid!");
				continue;
			}

			final String worldName = world.getName();

			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						// create prepared statement
						PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertDestination"));

						preparedStatement.setString(1, key);
						preparedStatement.setString(2, displayName);
						preparedStatement.setString(3, worldName);
						preparedStatement.setLong(4, destination.getWorldUid().getMostSignificantBits());
						preparedStatement.setLong(5, destination.getWorldUid().getLeastSignificantBits());
						preparedStatement.setDouble(6, destination.getX());
						preparedStatement.setDouble(7, destination.getY());
						preparedStatement.setDouble(8, destination.getZ());
						preparedStatement.setFloat(9, destination.getYaw());
						preparedStatement.setFloat(10, destination.getPitch());

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
			count++;
		}
		return count;
	}


	@Override
	public Destination selectRecord(final String key) {

		// if key is null return null record
		if (key == null) {
			return null;
		}

		// derive key in case destination name was passed
		String derivedKey = Destination.deriveKey(key);

		Destination destination = null;

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectDestination"));

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
				long worldUidMsb = rs.getLong("worldUidMsb");
				long worldUidLsb = rs.getLong("worldUidLsb");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");
				float yaw = rs.getFloat("yaw");
				float pitch = rs.getFloat("pitch");

				// reconstitute world uid from components
				UUID worldUid = new UUID(worldUidMsb, worldUidLsb);

				// get world
				World world = plugin.getServer().getWorld(worldUid);

				boolean worldValid = true;

				// if world is null, set worldValid false and log warning
				if (world == null) {
					worldValid = false;
					plugin.getLogger().warning("Stored destination has invalid world: " + worldName);
				}

				// create destination
				destination = new Destination(key, displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while fetching a destination from the SQLite database.");
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
	List<Destination> selectAllRecords() {

		List<Destination> returnList = new ArrayList<>();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllRecords"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				if (schemaVersion == 0) {
					String key = rs.getString("key");
					String displayName = rs.getString("displayname");
					String worldName = rs.getString("worldname");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");
					float yaw = rs.getFloat("yaw");
					float pitch = rs.getFloat("pitch");

					// get world by name
					World world = plugin.getServer().getWorld(worldName);

					boolean worldValid = true;

					UUID worldUid = null;

					// if world is null, set worldValid false and log warning
					if (world == null) {
						worldValid = false;
						plugin.getLogger().warning("Stored destination has invalid world: " + worldName);
					}
					else {
						// get world Uid
						worldUid = world.getUID();
					}

					// create destination from record
					Destination destination = new Destination(key, displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);

					// add destination to return list
					returnList.add(destination);
				}

				else if (schemaVersion == 1) {

					String key = rs.getString("key");
					String displayName = rs.getString("displayname");
					String worldName = rs.getString("worldname");
					long worldUidMsb = rs.getLong("worldUidMsb");
					long worldUidLsb = rs.getLong("worldUidLsb");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");
					float yaw = rs.getFloat("yaw");
					float pitch = rs.getFloat("pitch");

					// reconstitute world uid from components
					UUID worldUid = new UUID(worldUidMsb, worldUidLsb);

					// get world
					World world = plugin.getServer().getWorld(worldUid);

					boolean worldValid = true;

					// if world is null, set worldValid false and log warning
					if (world == null) {
						worldValid = false;
						plugin.getLogger().warning("Stored destination has invalid world: " + worldName);
					}

					// create destination
					Destination destination = new Destination(key, displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);

					// add destination to return list
					returnList.add(destination);
				}
			}
		}
		catch (SQLException e) {

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
	public List<String> selectAllKeys() {

		List<String> returnList = new ArrayList<>();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllKeys"));

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
	public Destination deleteRecord(String key) {

		// if key is null return null record
		if (key == null) {
			return null;
		}

		// derive key in case destination name was passed
		key = Destination.deriveKey(key);

		// get destination record to be deleted, for return
		Destination destination = this.selectRecord(key);

		try {
			// create prepared statement
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteDestination"));

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
