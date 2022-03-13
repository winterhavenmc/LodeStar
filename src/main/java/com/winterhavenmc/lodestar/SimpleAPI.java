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

package com.winterhavenmc.lodestar;

import com.winterhavenmc.lodestar.storage.Destination;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * LodeStar API
 *
 * @author Tim Savage
 * @version 1.0
 */
@SuppressWarnings("unused")
public final class SimpleAPI {

	// static reference to main class
	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	/**
	 * Private constructor to prevent instantiation
	 *
	 * @throws AssertionError on attempt to instantiate
	 */
	private SimpleAPI() {
		throw new AssertionError();
	}


	/**
	 * Create an item stack with encoded destination
	 *
	 * @param destinationName the destination name
	 * @return ItemStack with destination name and quantity
	 */
	public static ItemStack create(final String destinationName) {
		return plugin.lodeStarFactory.create(destinationName, 1);
	}


	/**
	 * Create an item stack with encoded destination and quantity
	 *
	 * @param destinationName the destination name
	 * @param quantity the quantity of items
	 * @return ItemStack with destination name and quantity
	 */
	public static ItemStack create(final String destinationName, final int quantity) {
		return plugin.lodeStarFactory.create(destinationName, quantity);
	}


	/**
	 * Set meta data on item stack
	 *
	 * @param itemStack       the ItemStack to encode with destination key
	 * @param destinationName the destination name used to create the encoded key
	 */
	public static void setMetaData(final ItemStack itemStack, final String destinationName) {
		plugin.lodeStarFactory.setMetaData(itemStack, destinationName);
	}


	/**
	 * Check if itemStack is a LodeStar item
	 *
	 * @param itemStack the ItemStack to test if LodeStar item
	 * @return boolean - {@code true} if ItemStack is LodeStar item, {@code false} if it is not
	 */
	public static boolean isLodeStar(final ItemStack itemStack) {
		return plugin.lodeStarFactory.isItem(itemStack);
	}


	/**
	 * Check if itemStack is a default LodeStar item
	 *
	 * @param itemStack the ItemStack to test if default LodeStar item
	 * @return boolean - {@code true} if ItemStack is a default LodeStar item, {@code false} if it is not
	 */
	public static boolean isDefaultItem(final ItemStack itemStack) {
		return plugin.lodeStarFactory.isDefaultItem(itemStack);
	}


	/**
	 * Get destination key encoded in item stack persistent meta data
	 *
	 * @param itemStack the item to get destination from
	 * @return String - the destination key
	 */
	public static String getDestination(final ItemStack itemStack) {
		return plugin.lodeStarFactory.getKey(itemStack);
	}


	/**
	 * Check if destination exists in storage or is reserved name
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination exists, {@code false} if it does not
	 */
	public static boolean isValidDestination(final String destinationName) {
		return Destination.exists(destinationName);
	}


	/**
	 * Check if a string is a valid destination name
	 * <ul>
	 *     <li>destination name must not be null</li>
	 *     <li>destination name must not be blank</li>
	 *     <li>destination name must not start with a digit</li>
	 *     <li>destination name must not contain a colon</li>
	 * </ul>
	 *
	 * @param destinationName the destination name to test for validity
	 * @return {@code true} if destination name is a valid name, {@code false} if it is not
	 */
	public static boolean isAllowedName(final String destinationName) {

		return !(destinationName == null
				|| destinationName.isBlank()
				|| destinationName.matches("^\\d.*")
				|| destinationName.matches(".*:.*"));
	}


	/**
	 * Check if destination name is a reserved name
	 *
	 * @param destinationName the destination name to test for reserved name
	 * @return {@code true} if destination is a reserved name, {@code false} if it is not
	 */
	static public boolean isReservedName(final String destinationName) {
		return Destination.isReserved(destinationName);
	}


	/**
	 * Check if passed destination name is reserved name for home location
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination name is reserved home name, {@code false} if not
	 */
	public static boolean isHomeName(final String destinationName) {
		return Destination.isHome(destinationName);
	}


	/**
	 * Check if passed destination name is reserved name for spawn location
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination name is reserved spawn name, {@code false} if not
	 */
	public static boolean isSpawnName(final String destinationName) {
		return Destination.isSpawn(destinationName);
	}


	/**
	 * Get item name from configuration file
	 *
	 * @return item name as specified in configuration file
	 */
	public static String getItemName() {
		return plugin.messageBuilder.getItemName();
	}


	/**
	 * Get destination name from passed key. Matching is case-insensitive.
	 *
	 * @param key the key for which to get destination name
	 * @return String name of destination, or null if no matching destination found
	 */
	public static String getDestinationName(final String key) {
		return Destination.getDisplayName(key);
	}


	/**
	 * get destination display name for persistent key stored in item stack<br>
	 * Matching is case-insensitive. Reserved names are tried first.
	 *
	 * @param itemStack the item stack from which to get name
	 * @return String destination display name, or null if no matching destination found
	 */
	public static String getDestinationName(final ItemStack itemStack) {

		// get persistent key from item stack
		String key = plugin.lodeStarFactory.getKey(itemStack);

		// if item stack persistent key is null, return null
		if (key == null) {
			return null;
		}

		String resultString = null;

		// if key matches spawn key, get spawn display name from language file
		if (Destination.isSpawn(key)) {
			resultString = plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn");
		}

		// if destination is home, get home display name from messages file
		else if (Destination.isHome(key)) {
			resultString = plugin.messageBuilder.getHomeDisplayName().orElse("Home");
		}

		// else get destination name from datastore
		else if (plugin.dataStore.selectRecord(key).isPresent()) {
			resultString = plugin.dataStore.selectRecord(key).get().getDisplayName();
		}

		return resultString;
	}


	/**
	 * Get configuration setting for allowing items to be used in recipes
	 *
	 * @return Boolean - {@code true} if items can be used in recipes, {@code false} if they cannot
	 */
	public static Boolean isValidIngredient() {
		return plugin.getConfig().getBoolean("allow-in-recipes");
	}


	/**
	 * Get configuration setting for cooldown time
	 *
	 * @return int - the cooldown time (in seconds) before another item can be used
	 */
	public static int getCooldownTime() {
		return plugin.getConfig().getInt("cooldown-time");
	}


	/**
	 * Get configuration setting for warmup time
	 *
	 * @return int - the warmup time (in seconds) before teleportation occurs when an item is used
	 */
	public static int getWarmupTime() {
		return plugin.getConfig().getInt("warmup-time");
	}


	/**
	 * Get configuration setting for minimum distance
	 *
	 * @return int - the minimum distance from a destination required to use an item
	 */
	public static int getMinDistance() {
		return plugin.getConfig().getInt("minimum-distance");
	}


	/**
	 * Get configuration setting for cancel on damage
	 *
	 * @return Boolean - {@code true} if teleportation will be cancelled when player takes damage
	 * during warmup time, {@code false} if not
	 */
	public static Boolean isCancelledOnDamage() {
		return plugin.getConfig().getBoolean("cancel-on-damage");
	}


	/**
	 * Get configuration setting for cancel on movement
	 *
	 * @return Boolean - {@code true} if teleportation will be cancelled when player moves
	 * during warmup time, {@code false} if not
	 */
	public static Boolean isCancelledOnMovement() {
		return plugin.getConfig().getBoolean("cancel-on-movement");
	}


	/**
	 * Get configuration setting for cancel on interaction
	 *
	 * @return Boolean - {@code true} if teleportation will be cancelled when player interacts with objects
	 * during warmup time, {@code false} if not
	 */
	public static Boolean isCancelledOnInteraction() {
		return plugin.getConfig().getBoolean("cancel-on-interaction");
	}


	/**
	 * Check if a player is currently warming up for item use
	 *
	 * @param player the player to check
	 * @return Boolean - {@code true} if player is warming up, {@code false} if not
	 */
	public static Boolean isWarmingUp(final Player player) {
		return plugin.teleportHandler.isWarmingUp(player);
	}


	/**
	 * Get List of String containing world names where plugin is enabled
	 *
	 * @return List of Strings containing enabled world names
	 */
	public static List<String> getEnabledWorlds() {
		return new ArrayList<>(plugin.worldManager.getEnabledWorldNames());
	}


	/**
	 * Get Collection of String containing world names where plugin is enabled
	 *
	 * @return Collection of Strings containing enabled world names
	 */
	public static Collection<String> getEnabledWorldsCollection() {
		return plugin.worldManager.getEnabledWorldNames();
	}


	/**
	 * Cancel a pending teleport for a player
	 *
	 * @param player the player to cancel teleporting
	 */
	public static void cancelTeleport(final Player player) {
		plugin.teleportHandler.cancelTeleport(player);
	}


	/**
	 * Create an itemStack with default material from config
	 *
	 * @return ItemStack
	 */
	public static ItemStack getDefaultItem() {

		// get configured material
		String configMaterial = plugin.getConfig().getString("default-material");

		// if no configuration for default material, set string to NETHER_STAR
		if (configMaterial == null) {
			configMaterial = "NETHER_STAR";
		}

		// try to match material
		Material material = Material.matchMaterial(configMaterial);

		// if no match, set material type to NETHER_STAR
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// create a one item stack with configured material
		return new ItemStack(material, 1);
	}


	/**
	 * Get location centered on x,z coordinates from a block location (where coordinates are integers)
	 *
	 * @param location the integer block location to center
	 * @return Location the centered on block location
	 */
	public static Location getBlockCenteredLocation(final Location location) {

		// if location is null, return null
		if (location == null) {
			return null;
		}

		final World world = location.getWorld();
		int x = location.getBlockX();
		int y = (int) Math.round(location.getY());
		int z = location.getBlockZ();
		return new Location(world, x + 0.5, y, z + 0.5, location.getYaw(), location.getPitch());
	}

}
