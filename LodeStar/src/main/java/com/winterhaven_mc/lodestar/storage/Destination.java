package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;

import org.bukkit.ChatColor;
import org.bukkit.Location;


public class Destination {

	private static final PluginMain plugin = PluginMain.instance;

	private final String key;
	private final String displayName;
	private final Location location;


	/**
	 * Class constructor
	 *
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(final String displayName, final Location location) {

		this.key = deriveKey(displayName);
		this.displayName = displayName;
		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
	}


	/**
	 * Class constructor
	 *
	 * @param key         the destination key value
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(final String key, final String displayName, final Location location) {

		this.key = key;
		this.displayName = displayName;
		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
	}


	/**
	 * Check if destination is spawn
	 *
	 * @return true if spawn, else false
	 */
	public boolean isSpawn() {
		return this.getKey().equals("spawn")
				|| this.getKey().equals(deriveKey(plugin.messageManager.getSpawnDisplayName()));
	}


	/**
	 * Check if destination is home
	 *
	 * @return true if home, else false
	 */
	@SuppressWarnings("unused")
	public boolean isHome() {
		return this.getKey().equals("home")
				|| this.getKey().equals(deriveKey(plugin.messageManager.getHomeDisplayName()));
	}


	/**
	 * Getter for destination key field
	 *
	 * @return the value of the key field
	 */
	String getKey() {
		return key;
	}


	/**
	 * Getter for destination displayName field
	 *
	 * @return the value of the displayName field
	 */
	public String getDisplayName() {
		return displayName;
	}


	/**
	 * Getter for destination location field
	 *
	 * @return the value of the location field
	 */
	public Location getLocation() {
		return location;
	}


	/**
	 * Derive key from destination display name<br>
	 * replaces spaces with underscores, strips color codes and folds to lower case
	 *
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
