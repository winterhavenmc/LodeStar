package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Destination {
	
	private static final PluginMain plugin = PluginMain.instance;

	private String key;
	private String displayName;
	private Location location;

	
	/**
	 * Class constructor
	 * @param displayName the destination display name string
	 * @param location the destination location
	 */
	public Destination(final String displayName, final Location location) {
		this.setKey(displayName);
		this.setDisplayName(displayName);
		this.setLocation(location);
	}
	
	
	/**
	 * Class constructor
	 * @param key the destination key value
	 * @param displayName the destination display name string
	 * @param location the destination location
	 */
	public Destination(final String key, final String displayName, final Location location) {
		this.setKey(key);
		this.setDisplayName(displayName);
		this.setLocation(location);
	}
	
	
	/**
	 * Check if destination is spawn
	 * @return true if spawn, else false
	 */
	public boolean isSpawn() {
		return this.getKey().equals("spawn")
				|| this.getKey().equals(deriveKey(plugin.messageManager.getSpawnDisplayName()));
	}
	
	
	/**
	 * Check if destination is home
	 * @return true if home, else false
	 */
	@SuppressWarnings("unused")
	public boolean isHome() {
		return this.getKey().equals("home")
				|| this.getKey().equals(deriveKey(plugin.messageManager.getHomeDisplayName()));
	}
	
	
	/**
	 * Getter for destination key field
	 * @return the value of the key field
	 */
	String getKey() {
		return key;
	}

	
	/**
	 * Setter for destination key field
	 * @param key the value to assign to the key field
	 */
	private void setKey(final String key) {
		this.key = deriveKey(key);
	}
	
	
	/**
	 * Getter for destination displayName field
	 * @return the value of the displayName field
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	
	/**
	 * Setter for destination displayName field
	 * @param displayName the value to assign to the displayName field
	 */
	private void setDisplayName(final String displayName) {
		this.displayName = displayName.replace('_', ' ');
	}
	
	
	/**
	 * Getter for destination location field
	 * @return the value of the location field
	 */
	public Location getLocation() {
		return location;
	}

	
	/**
	 * Setter for destination location field
	 * @param location value to assign to location field
	 */
	private void setLocation(final Location location) {
		this.location = location;
	}
	
	
	/**
	 * Derive key from destination display name<br>
	 * replaces spaces with underscores, strips color codes and folds to lower case
	 * @param key the destination name string to convert to a key value
	 * @return the key value derived from the destination name
	 */
	public static String deriveKey(final String key) {
		
		String derivedKey = key;
		
		derivedKey = derivedKey.replace(' ', '_');
		derivedKey = derivedKey.toLowerCase().replaceAll("[&" + ChatColor.COLOR_CHAR + "][0-9a-zA-Zk-oK-OrR]", "");
		return derivedKey;
	}
	
}
