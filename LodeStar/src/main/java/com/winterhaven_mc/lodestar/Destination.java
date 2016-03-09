package com.winterhaven_mc.lodestar;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Destination {
	
	private static final LodeStarMain plugin = LodeStarMain.instance;

	private String key;
	private String displayName;
	private Location location;

	
	/**
	 * Class constructor
	 * @param displayName
	 * @param location
	 */
	Destination(String displayName, Location location) {
		this.setKey(displayName);
		this.setDisplayName(displayName);
		this.setLocation(location);
	}
	
	
	/**
	 * Class constructor
	 * @param key
	 * @param displayName
	 * @param location
	 */
	Destination(String key, String displayName, Location location) {
		this.setKey(key);
		this.setDisplayName(displayName);
		this.setLocation(location);
	}
	
	
	/**
	 * Check if destination is spawn
	 * @return true if spawn, else false
	 */
	public boolean isSpawn() {
		if (this.getKey().equals("spawn") 
				|| this.getKey().equals(deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Check if destination is home
	 * @return true if home, else false
	 */
	public boolean isHome() {
		if (this.getKey().equals("home") 
				|| this.getKey().equals(deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Getter for destination key field
	 * @return
	 */
	public String getKey() {
		return key;
	}

	
	/**
	 * Setter for destination key field
	 * @param key
	 */
	public void setKey(String key) {
		this.key = deriveKey(key);
	}
	
	
	/**
	 * Getter for destination displayName field
	 * @return
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	
	/**
	 * Setter for destination displayName field
	 * @param displayName
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName.replace('_', ' ');
	}
	
	
	/**
	 * Getter for destination location field
	 * @return
	 */
	public Location getLocation() {
		return location;
	}

	
	/**
	 * Setter for destination location field
	 * @param location
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	
	/**
	 * Derive key from destination display name<br>
	 * replaces spaces with underscores, strips color codes and folds to lower case
	 * @param key
	 * @return
	 */
	public static String deriveKey(String key) {
		key = key.replace(' ', '_');
		key = key.toLowerCase().replaceAll("[&" + ChatColor.COLOR_CHAR + "][0-9a-zA-Zk-oK-OrR]", "");
		return key;
	}
	
}
