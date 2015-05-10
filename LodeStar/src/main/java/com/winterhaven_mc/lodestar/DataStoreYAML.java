package com.winterhaven_mc.lodestar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.World;

public class DataStoreYAML extends DataStore {

	private final LodeStarMain plugin; // reference to main class
	private ConfigAccessor destinationFile;
	
	// data store filename
	private static final String FILENAME = "destinations.yml";
	
	// data store type
	private static final DataStoreType TYPE = DataStoreType.YAML;
	

	DataStoreYAML (LodeStarMain plugin) {
		
		// reference to main class
		this.plugin = plugin;	
	}
	
	
	@Override
	void initialize() throws IllegalStateException, IllegalArgumentException {
		
		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			return;
		}
		
		// create new ConfigAccessor object
		destinationFile = new ConfigAccessor(plugin, FILENAME);
		
		// copy embedded default file if necessary
		destinationFile.saveDefaultConfig();
		
		// set initialized true
		setInitialized(true);
		if (plugin.debug) {
			plugin.getLogger().info("yaml datastore initialized.");
		}
	}


	@Override
	Destination getRecord(String key) {
		
		// if passed key is null return null record
		if (key == null) {
			return null;
		}
		
		// derive key in case destination name was passed
		key = Destination.deriveKey(key);
		
		// get default world
		List<World> allWorlds = plugin.getServer().getWorlds();
		World defaultWorld = allWorlds.get(0);
		
		// set location to default world spawn in case stored location is invalid
		Location location = defaultWorld.getSpawnLocation();
		
		// if destination is in data file, get data and return destination object
		if (destinationFile.getConfig().contains(key)) {
			
			// get stored displayName
			String displayName = destinationFile.getConfig().getString(key + ".display-name");
			// if no displayName stored, set to key
			if (displayName == null || displayName.isEmpty()) {
				displayName = key;
			}
			
			// get stored destination world name; return null record if world does not exist
			String worldName = destinationFile.getConfig().getString(key + ".world");
			
			if (plugin.getServer().getWorld(worldName) != null) {
				location.setWorld(plugin.getServer().getWorld(worldName));
			}
			else {
				return null;
			}
			
			// get location coordinates
			location.setX(destinationFile.getConfig().getDouble(key + ".x",0));
			location.setY(destinationFile.getConfig().getDouble(key + ".y",0));
			location.setZ(destinationFile.getConfig().getDouble(key + ".z",0));
			location.setPitch((float) destinationFile.getConfig().getDouble(key + ".pitch",0));
			location.setYaw((float) destinationFile.getConfig().getDouble(key + ".yaw",0));

			// return destination object
			return new Destination(displayName, location);
		}
		else {
			return null;
		}
	}
	
	@Override
	void putRecord(Destination destination) {
		
		// if destination is null do nothing and return
		if (destination == null) {
			return;
		}
		
		// get warp key
		String key = destination.getKey();
		
		// get warp display name
		String displayName = destination.getDisplayName();
		
		// if displayName is empty set to key
		if (displayName.isEmpty()) {
			displayName = key;
		}
		
		// get destination location
		Location location = destination.getLocation();
		
		// if world is not valid do nothing and return
		if (plugin.getServer().getWorld(location.getWorld().getName()) == null) {
			return;
		}
		
		// save display name in data file
		destinationFile.getConfig().set(key + ".display-name", displayName);
		
		// save location in data file
		destinationFile.getConfig().set(key + ".world", location.getWorld().getName());
		destinationFile.getConfig().set(key + ".x", location.getX());
		destinationFile.getConfig().set(key + ".y", location.getY());
		destinationFile.getConfig().set(key + ".z", location.getZ());
		destinationFile.getConfig().set(key + ".pitch", location.getPitch());
		destinationFile.getConfig().set(key + ".yaw", location.getYaw());
		
		// write in memory destination file to disk
		destinationFile.saveConfig();
	}
	
	@Override
	List<String> getAllKeys() {
		
		List<String> returnKeys = new ArrayList<String>();
		SortedSet<String> keys = new TreeSet<String>(destinationFile.getConfig().getKeys(false));
		for (String key : keys) {
			returnKeys.add(key);
		}
		return returnKeys;
	}
	
	List<String> getAllNames() {
		
		List<String> returnNames = new ArrayList<String>();
		SortedSet<String> keys = new TreeSet<String>(destinationFile.getConfig().getKeys(false));
		for (String key : keys) {
			returnNames.add(destinationFile.getConfig().getString(key + ".display-name"));
		}
		return returnNames;
	}
	
	@Override
	List<Destination> getAllRecords() {
		
		List<Destination> returnList = new ArrayList<Destination>();
		SortedSet<String> keys = new TreeSet<String>(destinationFile.getConfig().getKeys(false));
		for (String key : keys) {
			Destination record = this.getRecord(key);
			if (record == null) {
				continue;
			}
			returnList.add(this.getRecord(key));
		}	
		return returnList;
	}
	
	@Override
	Destination deleteRecord(String key) {
		
		// if key is null return null record
		if (key == null) {
			return null;
		}
		
		// fetch destination, to return
		Destination destination = this.getRecord(key);

		// delete destination from storage
		destinationFile.getConfig().set(Destination.deriveKey(key), null);
		
		// save in memory destination file to disk
		destinationFile.saveConfig();
		
		return destination;
	}

	@Override
	void close() {
		destinationFile.saveConfig();
		setInitialized(false);
	}
	
	@Override
	void save() {
		destinationFile.saveConfig();
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
		
		// get path name to this data store file
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
