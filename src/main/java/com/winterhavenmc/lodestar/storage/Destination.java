/*
 * Copyright (c) 2022 Tim Savage.
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

package com.winterhavenmc.lodestar.storage;

import com.winterhavenmc.lodestar.PluginMain;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;


public final class Destination {

	// static reference to plugin main class instance, necessary for static methods
	private static final PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private final String key;
	private final String displayName;
	private final boolean worldValid;
	private final String worldName;
	private final UUID worldUid;
	private final double x;
	private final double y;
	private final double z;
	private final float yaw;
	private final float pitch;

	/**
	 * Class constructor
	 *
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(final String displayName, final Location location) {

		// validate parameters
		Objects.requireNonNull(displayName);
		Objects.requireNonNull(location);

		this.key = deriveKey(displayName);
		this.displayName = displayName;

		if (location.getWorld() != null) {
			this.worldUid = location.getWorld().getUID();
			this.worldName = location.getWorld().getName();
			this.worldValid = true;
		}
		else {
			this.worldUid = null;
			this.worldName = "unknown";
			this.worldValid = false;
		}

		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}


	/**
	 * Class constructor
	 *
	 * @param key         the destination key value
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(final String key, final String displayName, final Location location) {

		// validate parameters
		Objects.requireNonNull(location);

		this.key = Objects.requireNonNull(key);
		this.displayName = Objects.requireNonNull(displayName);

		if (location.getWorld() != null) {
			this.worldUid = location.getWorld().getUID();
			this.worldName = location.getWorld().getName();
			this.worldValid = true;
		}
		else {
			this.worldUid = null;
			this.worldName = "unknown";
			this.worldValid = false;
		}

		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}


	/**
	 *
	 * @param key          the destination key
	 * @param displayName  the destination display name
	 * @param worldValid   destination world valid
	 * @param worldName    destination world name
	 * @param worldUid     destination world uid
	 * @param x            destination x coordinate
	 * @param y            destination y coordinate
	 * @param z            destination z coordinate
	 * @param yaw          destination yaw
	 * @param pitch        destination pitch
	 */
	public Destination(final String key,
					   final String displayName,
					   final boolean worldValid,
					   final String worldName,
					   final UUID worldUid,
					   final double x,
					   final double y,
					   final double z,
					   final float yaw,
					   final float pitch) {

		this.key = key;
		this.displayName = displayName;
		this.worldValid = worldValid;
		this.worldName = worldName;
		this.worldUid = worldUid;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}


	/**
	 * Get string representation of destination
	 * @return String - destination display name
	 */
	@Override
	public String toString() {
		return getDisplayName();
	}


	/**
	 * Check if destination is spawn location
	 *
	 * @return true if destination is spawn, else false
	 */
	public boolean isSpawn() {
		return this.getKey().equalsIgnoreCase("spawn")
				|| this.getKey().equals(deriveKey(plugin.messageBuilder.getSpawnDisplayName()));
	}


	/**
	 * Check if destination is home location
	 *
	 * @return true if home, else false
	 */
	@SuppressWarnings("unused")
	public boolean isHome() {
		return this.getKey().equalsIgnoreCase("home")
				|| this.getKey().equals(deriveKey(plugin.messageBuilder.getHomeDisplayName()));
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

		// if display name is null, return empty string
		if (displayName == null) {
			return "";
		}

		// return display name
		return displayName;
	}


	/**
	 * Getter for destination location field
	 *
	 * @return the value of the location field
	 */
	public Location getLocation() {

		// if world uid is null, return null
		if (worldUid == null) {
			return null;
		}

		// get world by uid
		World world = plugin.getServer().getWorld(worldUid);

		// if world is null, return null
		if (world == null) {
			return null;
		}

		// return new location object for destination
		return new Location(world, x, y, z, yaw, pitch);
	}


	public UUID getWorldUid() {
		return worldUid;
	}

	public String getWorldName() {
		return worldName;
	}

	public boolean isWorldValid() {
		return worldValid;
	}

	public double getX() {
		return x;
	}


	public double getY() {
		return y;
	}


	public double getZ() {
		return z;
	}


	public float getYaw() {
		return yaw;
	}


	public float getPitch() {
		return pitch;
	}


	/* STATIC METHODS */

	/**
	 * Derive key from destination display name<br>
	 * strips color codes and replaces spaces with underscores<br>
	 * if a destination key is passed, it will be returned unaltered
	 *
	 * @param destinationName the destination name to convert to a key
	 * @return String - the key derived from the destination name
	 */
	public static String deriveKey(final String destinationName) {

		// validate parameter
		Objects.requireNonNull(destinationName);

		// copy passed in destination name to derivedKey
		String derivedKey = destinationName;

		// translate alternate color codes
		derivedKey = ChatColor.translateAlternateColorCodes('&', derivedKey);

		// strip all color codes
		derivedKey = ChatColor.stripColor(derivedKey);

		// replace spaces with underscores
		derivedKey = derivedKey.replace(' ', '_');

		return derivedKey;
	}


	/**
	 * Check if destination exists in storage or is reserved name; accepts key or display name.<br>
	 * Matching is case-insensitive.
	 *
	 * @param key the destination name to check
	 * @return {@code true} if destination exists, {@code false} if it does not
	 */
	public static boolean exists(final String key) {

		// if parameter is null or empty string, return false
		if (key == null || key.isEmpty()) {
			return false;
		}

		String derivedKey = Destination.deriveKey(key);

		return isReserved(key) || plugin.dataStore.selectRecord(derivedKey).isPresent();
	}


	/**
	 * Check if destination key or display name is a reserved name<br>
	 * Matching is case-insensitive.
	 *
	 * @param key the key or destination name to test for reserved name
	 * @return {@code true} if destination is a reserved name, {@code false} if it is not
	 */
	public static boolean isReserved(final String key) {

		// if parameter is null or empty string, return false
		if (key == null || key.isEmpty()) {
			return false;
		}

		// test if passed destination name is reserved name for home or spawn locations
		return isHome(key) || isSpawn(key);
	}


	/**
	 * Check if passed key is reserved name for home location; accepts key or display name<br>
	 * Matching is case-insensitive.
	 *
	 * @param key the destination name to check
	 * @return {@code true} if destination name is reserved home name, {@code false} if not
	 */
	public static boolean isHome(final String key) {

		// if parameter is null or empty string, return false
		if (key == null || key.isEmpty()) {
			return false;
		}

		// derive key from destination name to normalize string (strip colors, replace spaces with underscores)
		String derivedKey = Destination.deriveKey(key);
		return derivedKey.equalsIgnoreCase("home")
				|| derivedKey.equalsIgnoreCase(Destination.deriveKey(plugin.messageBuilder.getHomeDisplayName()));
	}


	/**
	 * Check if passed key is reserved name for spawn location; accepts key or display name.<br>
	 * Matching is case-insensitive.
	 *
	 * @param key the destination name to check
	 * @return {@code true} if destination name is reserved spawn name, {@code false} if not
	 */
	public static boolean isSpawn(final String key) {

		// if parameter is null or empty string, return false
		if (key == null || key.isEmpty()) {
			return false;
		}

		// derive key from destination name to normalize string (strip colors, replace spaces with underscores)
		String derivedKey = Destination.deriveKey(key);
		return derivedKey.equalsIgnoreCase("spawn")
				|| derivedKey.equalsIgnoreCase(Destination.deriveKey(plugin.messageBuilder.getSpawnDisplayName()));
	}


	/**
	 * Get destination display name from destination represented by passed key.
	 * Accepts key or display name.<br>
	 * Matching is case-insensitive. Reserved names are tried first.
	 *
	 * @param key the key of the destination for which to retrieve display name
	 * @return String - the formatted display name for the destination, or null if no record exists
	 */
	public static String getDisplayName(final String key) {

		// if key matches spawn key, get spawn display name from messages files
		if (isSpawn(key)) {
			return plugin.messageBuilder.getSpawnDisplayName();
		}

		// if key matches home key, get home display name from messages file
		if (isHome(key)) {
			return plugin.messageBuilder.getHomeDisplayName();
		}

		// else try to get destination name from datastore
		return plugin.dataStore.selectRecord(Destination.deriveKey(key)).map(Destination::getDisplayName).orElse(null);
	}


	/**
	 * get destination display name for persistent key stored in item stack<br>
	 * Matching is case-insensitive. Reserved names are tried first.
	 *
	 * @param itemStack the item stack from which to get name
	 * @return String destination display name, or null if no matching destination found
	 */
	public static String getDisplayName(final ItemStack itemStack) {

		// get persistent key from item stack
		String key = plugin.lodeStarFactory.getKey(itemStack);

		// if item stack persistent key is null, return null
		if (key == null) {
			return null;
		}

		String resultString = null;

		// if key matches spawn key, get spawn display name from language file
		if (isSpawn(key)) {
			resultString = plugin.messageBuilder.getSpawnDisplayName();
		}

		// if destination is home, get home display name from messages file
		else if (isHome(key)) {
			resultString = plugin.messageBuilder.getHomeDisplayName();
		}

		// else get destination name from datastore
		else if (plugin.dataStore.selectRecord(key).isPresent()) {
			resultString = plugin.dataStore.selectRecord(key).get().getDisplayName();
		}

		return resultString;
	}

}
