package com.winterhaven_mc.lodestar;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Destination {
	
	private static final PluginMain plugin = PluginMain.instance;

	private String key;
	private String displayName;
	private Location location;

	
	/**
	 * Class constructor
	 * @param displayName
	 * @param location
	 */
	Destination(final String displayName, final Location location) {
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
	Destination(final String key, final String displayName, final Location location) {
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
	public void setKey(final String key) {
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
	public void setDisplayName(final String displayName) {
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
	public void setLocation(final Location location) {
		this.location = location;
	}
	
	
	/**
	 * Derive key from destination display name<br>
	 * replaces spaces with underscores, strips color codes and folds to lower case
	 * @param derivedKey
	 * @return
	 */
	public static String deriveKey(final String key) {
		
		String derivedKey = key;
		
		derivedKey = derivedKey.replace(' ', '_');
		derivedKey = derivedKey.toLowerCase().replaceAll("[&" + ChatColor.COLOR_CHAR + "][0-9a-zA-Zk-oK-OrR]", "");
		return derivedKey;
	}
	
}
