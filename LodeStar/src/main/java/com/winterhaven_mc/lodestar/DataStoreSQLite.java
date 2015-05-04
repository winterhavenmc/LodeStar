package com.winterhaven_mc.lodestar;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;


public class DataStoreSQLite extends DataStore {

	// reference to main class
	private final LodeStarMain plugin;
	
	// database connection object
	private Connection connection;
	
	// data store file name
	private static final String FILENAME = "destinations.db";

	// data store type
	private static final DataStoreType TYPE = DataStoreType.SQLITE;
	
	DataStoreSQLite (LodeStarMain plugin) {

		// reference to main class
		this.plugin = plugin;	
	}
	
	
	@Override
	void initialize() throws SQLException, ClassNotFoundException {
		
		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			if (plugin.debug) {
				plugin.getLogger().info("sqlite datastore already initialized.");
			}
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
		String destinationsDb = plugin.getDataFolder() + File.separator + FILENAME;
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
			plugin.getLogger().info("sqlite datastore initialized.");
		}

	}


	@Override
	Destination getRecord(String key) {
		
		// if key is null return null record
		if (key == null) {
			return null;
		}
		
		// derive key in case destination name was passed
		key = Destination.deriveKey(key);
		
		Destination destination = null;
		World world = null;
		
		final String sqlGetDestination = "SELECT * FROM destinations WHERE key = ?";

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sqlGetDestination);
			
			preparedStatement.setString(1, key);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (rs.next()) {
			
				// get stored displayName
				String displayName = rs.getString("displayname");
				if (displayName == null || displayName.isEmpty()) {
					displayName = key;
				}
				
				// get stored world and coordinates
				String worldName = rs.getString("worldname");
				Double x = rs.getDouble("x");
				Double y = rs.getDouble("y");
				Double z = rs.getDouble("z");
				Float yaw = rs.getFloat("yaw");
				Float pitch = rs.getFloat("pitch");
				
				if (plugin.getServer().getWorld(worldName) == null) {
					plugin.getLogger().warning("Stored destination world not found!");
					return null;
				}
				world = plugin.getServer().getWorld(worldName);
				Location location = new Location(world,x,y,z,yaw,pitch);
				destination = new Destination(key,displayName,location);
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
	void putRecord(Destination destination) {
		
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
		String testWorldName = null;

		// test that world in destination location is valid
		try {
			testWorldName = location.getWorld().getName();
		} catch (Exception e) {
			plugin.getLogger().warning("An error occured while inserting"
					+ " a destination in the SQLite database. World invalid!");
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
			plugin.getLogger().warning("An error occured while inserting a destination into the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}
	}
	
	@Override
	List<String> getAllKeys() {
		
		List<String> returnList = new ArrayList<String>();

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
			plugin.getLogger().warning("An error occurred while trying to fetch all records from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return results
		return returnList;
	}
	
	List<Destination> getAllRecords() {
		
		List<Destination> returnList = new ArrayList<Destination>();

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
				Double x = rs.getDouble("x");
				Double y = rs.getDouble("y");
				Double z = rs.getDouble("z");
				Float yaw = rs.getFloat("yaw");
				Float pitch = rs.getFloat("pitch");
				
				World world;
				
				try {
					world = plugin.getServer().getWorld(worldName);
				} catch (Exception e) {
					plugin.getLogger().warning("Stored destination has unloaded world: " 
							+ worldName + ". Skipping record.");
					continue;
				}
				
				Location location = new Location(world,x,y,z,yaw,pitch);				
				Destination destination = new Destination(key, displayName, location);
				returnList.add(destination);
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to fetch all records from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output sql error message
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return results
		return returnList;
		
	}
	
	@Override
	Destination deleteRecord(String key) {
		
		// if key is null return null record
		if (key == null) {
			return null;
		}

		// derive key in case destination name was passed
		key = Destination.deriveKey(key);
		
		// get destination record to be deleted, for return
		Destination destination = this.getRecord(key);

		final String sqlDeleteDestination = "DELETE FROM destinations "
				+ "WHERE key = ?";
		
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
			plugin.getLogger().warning("An error occurred while attempting to delete a destination from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output sql error message
			if (plugin.debug) {
				e.getStackTrace();
			}
		}		
		return destination;
	}

	@Override
	void close() {

		try {
			connection.close();
			plugin.getLogger().info("SQLite database connection closed.");		
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while closing the SQLite database connection.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output sql error message
			if (plugin.debug) {
				e.getStackTrace();
			}
		}
		setInitialized(false);
	}
	
	@Override
	void save() {
	
		// no action necessary for this storage type
		
	}
	
	@Override
	void delete() {
		
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			dataStoreFile.delete();
		}
	}
	
	@Override
	boolean exists() {
		
		// get path name to old data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();

	}
	
	@Override
	String getFilename() {
		return FILENAME;
	}
	
	@Override
	DataStoreType getType() {
		return TYPE;
	}

}
