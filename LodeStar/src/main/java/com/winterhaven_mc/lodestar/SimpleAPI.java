package com.winterhaven_mc.lodestar;

import com.winterhaven_mc.lodestar.storage.Destination;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements LodeStarAPI.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
@SuppressWarnings("unused")
public final class SimpleAPI {

	// static reference to main class
	private final static PluginMain plugin = PluginMain.instance;

	// create item tag string
	private final static String itemTag = plugin.messageManager.createHiddenString("SSv2");

	// private constructor to prevent instantiation
	private SimpleAPI() {

	}

	
	/**
	 * Create an item stack with encoded destination and quantity
	 * @param destinationName the destination name
	 * @param qty the quantity of items
	 * @return ItemStack with destination name and quantity
	 */
	@SuppressWarnings("unused")
	public static ItemStack createItem(final String destinationName, final int qty) {

		// validate quantity
		int quantity = Math.max(qty, 1);

		// create item stack with configured material and data
		ItemStack newItem = getDefaultItem();

		// set quantity
		newItem.setAmount(quantity);
		
		// set item display name and lore
		setMetaData(newItem, destinationName);
		
		// return new item
		return newItem;
	}
	
	
	/**
	 * Encode hidden destination key in item lore
	 * @param itemStack the ItemStack to encode with destination key
	 * @param destinationName the destination name used to create the encoded key
	 */
	public static void setMetaData(final ItemStack itemStack, final String destinationName) {
		
		// get key from destination name parameter
		String key = Destination.deriveKey(destinationName);
		
		// get formatted destination name
		String formattedName = getDestinationName(key);
		
		// retrieve item name and lore from language file file
		String displayName = plugin.messageManager.getInventoryItemName();
		//noinspection unchecked
		List<String> configLore = plugin.messageManager.getItemLore();

		// substitute %destination_name% variable in display name
		displayName = displayName.replaceAll("%destination_name%", formattedName);
		displayName = displayName.replaceAll("%DESTINATION_NAME%", ChatColor.stripColor(formattedName));

		// allow for '&' character for color codes in name and lore
		displayName = ChatColor.translateAlternateColorCodes('&', displayName);

		ArrayList<String> coloredLore = new ArrayList<>();
		
		for (String line : configLore) {
			line = line.replaceAll("%destination_name%", formattedName);
			line = line.replaceAll("%DESTINATION_NAME%", ChatColor.stripColor(formattedName));
			coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		String hiddenDestination = plugin.messageManager.createHiddenString(key);
		String hiddenSeparator = plugin.messageManager.createHiddenString("|");
		
		// set invisible tag in first line of lore
		coloredLore.set(0, itemTag + hiddenSeparator + hiddenDestination + hiddenSeparator + coloredLore.get(0));

		// get item metadata object
		ItemMeta itemMeta = itemStack.getItemMeta();
		
		// set item metadata display name to value from config file
		itemMeta.setDisplayName(displayName);
		
		// set item metadata Lore to value from config file
		itemMeta.setLore(coloredLore);
		
		// save new item metadata
		itemStack.setItemMeta(itemMeta);
	}
	
	
	/**
	 * Check if itemStack is a LodeStar item
	 * @param itemStack the ItemStack to test if LodeStar item
	 * @return {@code true} if ItemStack is LodeStar item, {@code false} if it is not
	 */
	public static boolean isLodeStar(final ItemStack itemStack) {
		
		// if item stack is empty (null or air) return false
		if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
			return false;
		}
				
		// if item stack does not have lore return false
		if (!itemStack.getItemMeta().hasLore()) {
			return false;
		}
		
		// get item lore
		List<String> itemLore = itemStack.getItemMeta().getLore();
		
		// check that lore contains hidden token
		return !itemLore.isEmpty() && itemLore.get(0).startsWith(itemTag);
	}

	
	/**
	 * Check if itemStack is a default LodeStar item
	 * @param itemStack the ItemStack to test if default LodeStar item
	 * @return {@code true} if ItemStack is a default LodeStar item, {@code false} if it is not
	 */
	public static boolean isDefaultItem(final ItemStack itemStack) {
		
		if (plugin.debug) {
			plugin.getLogger().info("isDefaultItem: " + itemStack.toString());
		}
		
		// if item stack is empty (null or air) return false
		if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
			return false;
		}

		// try to match material
		Material material = Material.matchMaterial(plugin.getConfig().getString("default-material"));

		// if no match set to nether star
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// if material and data match defaults return true
		return itemStack.getType().equals(material);
	}

	
	/**
	 * Gets destination key from itemStack
	 * @param itemStack the ItemStack from which to retrieve a destination key
	 * @return destination key or null if item has no key
	 */
	public static String getKey(final ItemStack itemStack) {
		
		String destinationKey = null;
		
		// check that item has lore
		if (!itemStack.getItemMeta().hasLore()) {
			return null;
		}
		
		List<String> itemLore = itemStack.getItemMeta().getLore();
		
		if (itemLore.get(0) != null) {
			destinationKey = itemLore.get(0).replaceFirst("^" + itemTag + ChatColor.COLOR_CHAR + "\\|", "");
			destinationKey = destinationKey.replaceFirst(ChatColor.COLOR_CHAR + "\\|" + ".*$","");
			destinationKey = destinationKey.replace("" + ChatColor.COLOR_CHAR, "");
		}
		return destinationKey;
	}

	
	/**
	 * Check if destination exists in storage or is reserved name
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination exists, {@code false} if it does not
	 */
	public static boolean isValidDestination(final String destinationName) {
		
		if (destinationName == null || destinationName.isEmpty()) {
			return false;
		}
		
		String key = Destination.deriveKey(destinationName);

		return isReservedName(destinationName) || plugin.dataStore.getRecord(key) != null;
	}

	
	/**
	 * Check if destination is an allowable string
	 * @param destinationName the destination name to test for validity
	 * @return {@code true} if destination name is a valid name, {@code false} if it is not
	 */
	@SuppressWarnings("unused")
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
	 * @param destinationName the destination name to test for reserved name
	 * @return {@code true} if destination is a reserved name, {@code false} if it is not
	 */
	static public boolean isReservedName(final String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		return isSpawnName(key) || isHomeName(key);
	}

	
	@SuppressWarnings("WeakerAccess")
	public static boolean isHomeName(final String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		return key.equals("home")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()));
	}

	
	@SuppressWarnings("WeakerAccess")
	public static boolean isSpawnName(final String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		return key.equals("spawn")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()));
	}

	
	
	@SuppressWarnings("unused")
	public static String getItemName() {
		return plugin.messageManager.getItemName();
	}
	
	
	public static String getDestinationName(final String key) {

		String returnName = key;
		String derivedKey = Destination.deriveKey(key);
		
		// if destination is spawn get spawn display name from messages files
		if (derivedKey.equals("spawn") 
				|| derivedKey.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			returnName = plugin.messageManager.getSpawnDisplayName();
		}
		
		// if destination is home get home display name from messages file
		else if (derivedKey.equals("home") 
				|| derivedKey.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			returnName = plugin.messageManager.getHomeDisplayName();
		}
		
		// else get destination name from datastore
		else {
			Destination destination = plugin.dataStore.getRecord(derivedKey);
			if (destination != null) {
				returnName = destination.getDisplayName();
			}
		}
		
		// if no destination name found, use derived key for name
		if (returnName == null) {
			returnName = derivedKey;
		}		
		
		return returnName;
	}
	
	public static String getDestinationName(final ItemStack itemStack) {
		
		String key = getKey(itemStack);
		String destinationName = null;
		
		// if destination is spawn get spawn display name from messages files
		if (key != null) {
			if (key.equals("spawn") || key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
                destinationName = plugin.messageManager.getSpawnDisplayName();
            }
            // if destination is home get home display name from messages file
            else if (key.equals("home")
                    || key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
                destinationName = plugin.messageManager.getHomeDisplayName();
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
	
	public static Boolean isValidIngredient() {
		return plugin.getConfig().getBoolean("allow-in-recipes");
	}
	
	public static int getCooldownTime() {
		return plugin.getConfig().getInt("cooldown-time");
	}

	public static int getWarmupTime() {
		return plugin.getConfig().getInt("warmup-time");
	}

	public static int getMinDistance() {
		return plugin.getConfig().getInt("minimum-distance");
	}

	public static Boolean isCancelledOnDamage() {
		return plugin.getConfig().getBoolean("cancel-on-damage");
	}

	public static Boolean isCancelledOnMovement() {
		return plugin.getConfig().getBoolean("cancel-on-movement");
	}

	public static Boolean isCancelledOnInteraction() {
		return plugin.getConfig().getBoolean("cancel-on-interaction");
	}
	
	public static Boolean isWarmingUp(final Player player) {
		return plugin.teleportManager.isWarmingUp(player);
	}
	
	public static Boolean isCoolingDown(final Player player) {
		return plugin.teleportManager.getCooldownTimeRemaining(player) > 0;
	}
	
	public static long cooldownTimeRemaining(final Player player) {
		return plugin.teleportManager.getCooldownTimeRemaining(player);
	}
	
	public static List<String> getEnabledWorlds() {
		return plugin.worldManager.getEnabledWorldNames();
	}
	
	public static void cancelTeleport(final Player player) {
		plugin.teleportManager.cancelTeleport(player);
	}

	
	/**
	 * Create an itemStack with default material and data from config
	 * @return ItemStack
	 */
	@SuppressWarnings("WeakerAccess")
	public static ItemStack getDefaultItem() {
		
		// try to match material type to string in config file
		Material configMaterial = Material.matchMaterial(plugin.getConfig().getString("default-material"));
		
		// if no match default to nether star
		if (configMaterial == null) {
			configMaterial = Material.NETHER_STAR;
		}

		// create a one item stack with configured material
		return new ItemStack(configMaterial,1);
	}

	
	static Location getBlockCenteredLocation(final Location location) {
		
		// if location is null, return null
		if (location == null) {
			return null;
		}
		
		final World world = location.getWorld();
		int x = location.getBlockX();
		int y = (int)Math.round(location.getY());
		int z = location.getBlockZ();
		return new Location(world, x + 0.5, y, z + 0.5, location.getYaw(), location.getPitch());
	}

}

