package com.winterhaven_mc.lodestar;

import com.winterhaven_mc.lodestar.storage.Destination;

import com.winterhaven_mc.lodestar.util.LodeStar;
import com.winterhaven_mc.util.LanguageManager;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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

	// static reference to language manager
	private final static LanguageManager languageManager = LanguageManager.getInstance();

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
	 * @deprecated use {@code LodeStar.create()} method
	 */
	public static ItemStack create(final String destinationName) {
		return LodeStar.create(destinationName, 1);
	}


	/**
	 * Create an item stack with encoded destination and quantity
	 *
	 * @param destinationName the destination name
	 * @param quantity the quantity of items
	 * @return ItemStack with destination name and quantity
	 * @deprecated use {@code LodeStar.create()} method
	 */
	public static ItemStack create(final String destinationName, final int quantity) {
		return LodeStar.create(destinationName, quantity);
	}


	/**
	 * Set meta data on item stack
	 *
	 * @param itemStack       the ItemStack to encode with destination key
	 * @param destinationName the destination name used to create the encoded key
	 * @deprecated use {@code LodeStar.setMetaData()} method
	 */
	public static void setMetaData(final ItemStack itemStack, final String destinationName) {
		LodeStar.setMetaData(itemStack, destinationName);
	}


	/**
	 * Check if itemStack is a LodeStar item
	 *
	 * @param itemStack the ItemStack to test if LodeStar item
	 * @return boolean - {@code true} if ItemStack is LodeStar item, {@code false} if it is not
	 * @deprecated use {@code LodeStar.isItem()} method
	 */
	public static boolean isLodeStar(final ItemStack itemStack) {
		return LodeStar.isItem(itemStack);
	}


	/**
	 * Check if itemStack is a default LodeStar item
	 *
	 * @param itemStack the ItemStack to test if default LodeStar item
	 * @return boolean - {@code true} if ItemStack is a default LodeStar item, {@code false} if it is not
	 * @deprecated use {@code LodeStar.isDefaultItem(itemStack)} method
	 */
	public static boolean isDefaultItem(final ItemStack itemStack) {
		return LodeStar.isDefaultItem(itemStack);
	}


	/**
	 * Get destination key encoded in item stack persistent meta data
	 *
	 * @param itemStack the item to get destination from
	 * @return String - the destination key
	 * @deprecated use {@code LodeStar.getKey(itemStack)} method
	 */
	public static String getDestination(final ItemStack itemStack) {
		return LodeStar.getKey(itemStack);
	}


	/**
	 * Check if destination exists in storage or is reserved name
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination exists, {@code false} if it does not
	 * @deprecated use {@code Destination.isValid()} method
	 */
	public static boolean isValidDestination(final String destinationName) {
		return Destination.exists(destinationName);
	}


	/**
	 * Check if a string is an valid destination name
	 *
	 * @param destinationName the destination name to test for validity
	 * @return {@code true} if destination name is a valid name, {@code false} if it is not
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static boolean isAllowedName(final String destinationName) {

		// destination name cannot be null
		// destination name cannot be empty
		// destination name cannot start with a digit
		// destination name cannot contain a colon

		return !(destinationName == null
				|| destinationName.isEmpty()
				|| destinationName.matches("^\\d.*")
				|| destinationName.matches(".*:.*"));
	}


	/**
	 * Check if destination name is a reserved name
	 *
	 * @param destinationName the destination name to test for reserved name
	 * @return {@code true} if destination is a reserved name, {@code false} if it is not
	 * @deprecated use {@code Destination.isReserved()} method
	 */
	static public boolean isReservedName(final String destinationName) {
		return Destination.isReserved(destinationName);
	}


	/**
	 * Check if passed destination name is reserved name for home location
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination name is reserved home name, {@code false} if not
	 * @deprecated this method is likely to be removed from this plugin
	 */
	public static boolean isHomeName(final String destinationName) {
		return Destination.isHome(destinationName);
	}


	/**
	 * Check if passed destination name is reserved name for spawn location
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination name is reserved spawn name, {@code false} if not
	 * @deprecated this method is likely to be removed from this plugin
	 */
	public static boolean isSpawnName(final String destinationName) {

		// derive key from destination name to normalize string (strip colors, fold to lowercase, etc)
		String key = Destination.deriveKey(destinationName);
		return key.equals("spawn")
				|| key.equals(Destination.deriveKey(languageManager.getSpawnDisplayName()));
	}


	/**
	 * Get item name from configuration file
	 *
	 * @return item name as specified in configuration file
	 * @deprecated use {@code LodeStar.getItemName()} method
	 */
	public static String getItemName() {
		return languageManager.getItemName();
	}


	/**
	 * Get destination name from passed key
	 *
	 * @param key the key for which to get destination name
	 * @return String name of destination
	 * @deprecated use {@code Destination.getName(key)} method
	 */
	public static String getDestinationName(final String key) {
		return Destination.getName(key);
	}


	/**
	 * get destination name from passed item stack
	 *
	 * @param itemStack the item stack from which to get name
	 * @return String destination name
	 * @deprecated use {@code Destination.getName(itemStack)} method
	 */
	@Deprecated
	public static String getDestinationName(final ItemStack itemStack) {

		String key = LodeStar.getKey(itemStack);
		String destinationName = null;

		// if destination is spawn get spawn display name from messages files
		if (key != null) {
			if (key.equals("spawn") || key.equals(Destination.deriveKey(languageManager.getSpawnDisplayName()))) {
				destinationName = languageManager.getSpawnDisplayName();
			}
			// if destination is home get home display name from messages file
			else if (key.equals("home")
					|| key.equals(Destination.deriveKey(languageManager.getHomeDisplayName()))) {
				destinationName = languageManager.getHomeDisplayName();
			}
			// else get destination name from datastore
			else {
				Destination destination = plugin.dataStore.getRecord(key);
				if (destination != null) {
					destinationName = destination.getDisplayName();
				}
			}
		}
		// if no destination name found, use key for name
		if (destinationName == null) {
			destinationName = key;
		}
		return destinationName;
	}


	/**
	 * Get configuration setting for allowing items to be used in recipes
	 *
	 * @return Boolean - {@code true} if items can be used in recipes, {@code false} if they cannot
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static Boolean isValidIngredient() {
		return plugin.getConfig().getBoolean("allow-in-recipes");
	}


	/**
	 * Get configuration setting for cooldown time
	 *
	 * @return int - the cooldown time (in seconds) before another item can be used
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static int getCooldownTime() {
		return plugin.getConfig().getInt("cooldown-time");
	}


	/**
	 * Get configuration setting for warmup time
	 *
	 * @return int - the warmup time (in seconds) before teleportation occurs when an item is used
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static int getWarmupTime() {
		return plugin.getConfig().getInt("warmup-time");
	}


	/**
	 * Get configuration setting for minimum distance
	 *
	 * @return int - the minimum distance from a destination required to use an item
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static int getMinDistance() {
		return plugin.getConfig().getInt("minimum-distance");
	}


	/**
	 * Get configuration setting for cancel on damage
	 *
	 * @return Boolean - {@code true} if teleportation will be cancelled when player takes damage
	 * during warmup time, {@code false} if not
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static Boolean isCancelledOnDamage() {
		return plugin.getConfig().getBoolean("cancel-on-damage");
	}


	/**
	 * Get configuration setting for cancel on movement
	 *
	 * @return Boolean - {@code true} if teleportation will be cancelled when player moves
	 * during warmup time, {@code false} if not
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static Boolean isCancelledOnMovement() {
		return plugin.getConfig().getBoolean("cancel-on-movement");
	}


	/**
	 * Get configuration setting for cancel on interaction
	 *
	 * @return Boolean - {@code true} if teleportation will be cancelled when player interacts with objects
	 * during warmup time, {@code false} if not
	 * @deprecated this method will likely be removed from future versions of this plugin
	 */
	public static Boolean isCancelledOnInteraction() {
		return plugin.getConfig().getBoolean("cancel-on-interaction");
	}


	/**
	 * Check if a player is currently warming up for item use
	 *
	 * @param player the player to check
	 * @return Boolean - {@code true} if player is warming up, {@code false} if not
	 * @deprecated use {@code teleportManager.isWarmingUp(player)} method
	 */
	public static Boolean isWarmingUp(final Player player) {
		return plugin.teleportManager.isWarmingUp(player);
	}


	/**
	 * Check if a player is currently cooling down for item use
	 *
	 * @param player the player to check
	 * @return Boolean - {@code true} if player is cooling down, {@code false} if not
	 * @deprecated use {@code teleportManager.isCooling(player)} method
	 */
	public static Boolean isCoolingDown(final Player player) {
		return plugin.teleportManager.isCoolingDown(player);
	}


	/**
	 * Get a player's cooldown time remaining for item use
	 *
	 * @param player the player to check
	 * @return long - cooldown time remaining in milliseconds
	 * @deprecated use {@code teleportManager.getCooldownTimeRemaining(player)} method
	 */
	public static long cooldownTimeRemaining(final Player player) {
		return plugin.teleportManager.getCooldownTimeRemaining(player);
	}


	/**
	 * Get List of String containing worlds where plugin is enabled
	 *
	 * @return List of Strings containing enabled world names
	 * @deprecated use {@code worldManager.getEnabledWorldNames()} method
	 */
	public static List<String> getEnabledWorlds() {
		return plugin.worldManager.getEnabledWorldNames();
	}


	/**
	 * Cancel a pending teleport for a player
	 *
	 * @param player the player to cancel teleporting
	 * @deprecated use {@code teleportManager.cancelTeleport(player)} method
	 */
	public static void cancelTeleport(final Player player) {
		plugin.teleportManager.cancelTeleport(player);
	}


	/**
	 * Create an itemStack with default material from config
	 *
	 * @return ItemStack
	 * @deprecated this method will likely be removed from future versions of this plugin
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
	 * Get location centered on x,z coordinates from a block location (where coords are integers)
	 *
	 * @param location the integer block location to center
	 * @return Location the centered on block location
	 * @deprecated this method will be removed from future versions of this plugin
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
