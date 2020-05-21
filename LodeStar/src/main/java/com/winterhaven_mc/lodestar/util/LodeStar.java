package com.winterhaven_mc.lodestar.util;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.storage.Destination;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;


public final class LodeStar {

	// static reference to main class instance
	private final static PluginMain plugin = PluginMain.instance;

	// name spaced key for persistent data
	public final static NamespacedKey PERSISTENT_KEY = new NamespacedKey(plugin, "destination");


	/**
	 * Private constructor to prevent instantiation
	 *
	 * @throws AssertionError on attempt to instantiate
	 */
	private LodeStar() {
		throw new AssertionError();
	}


	/**
	 * Create an item stack with encoded destination
	 *
	 * @param destinationName the destination name
	 * @return ItemStack with destination name and quantity
	 */
	@SuppressWarnings("unused")
	public static ItemStack create(final String destinationName) {
		return create(destinationName, 1);
	}


	/**
	 * Create an item stack with encoded destination
	 *
	 * @param destinationName the destination name
	 * @param quantity the number of items in the stack
	 * @return ItemStack with destination name and quantity
	 */
	public static ItemStack create(final String destinationName, final int quantity) {

		// validate quantity is at least one
		int newQuantity = Math.max(1, quantity);

		// if configured max give quantity is positive, validate quantity
		if (plugin.getConfig().getInt("max-give-amount") > 0) {
			newQuantity = Math.min(plugin.getConfig().getInt("max-give-amount"), newQuantity);
		}

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

		// create item stack with configured material
		ItemStack newItem = new ItemStack(material, newQuantity);

		// set item display name and lore
		setMetaData(newItem, destinationName);

		// return new item
		return newItem;
	}


	/**
	 * Set meta data on item stack
	 *
	 * @param itemStack       the ItemStack to encode with destination key
	 * @param destinationName the destination name used to create the encoded key
	 */
	public static void setMetaData(final ItemStack itemStack, final String destinationName) {

		// retrieve item name and lore from language file file
		String displayName = plugin.messageManager.getInventoryItemName();
		List<String> configLore = plugin.messageManager.getItemLore();

		// substitute %destination_name% variable in display name
		displayName = displayName.replaceAll("%destination_name%", destinationName);
		displayName = displayName.replaceAll("%DESTINATION_NAME%", ChatColor.stripColor(destinationName));

		// allow for '&' character for color codes in name and lore
		displayName = ChatColor.translateAlternateColorCodes('&', displayName);

		ArrayList<String> coloredLore = new ArrayList<>();

		for (String line : configLore) {
			line = line.replaceAll("%destination_name%", destinationName);
			line = line.replaceAll("%DESTINATION_NAME%", ChatColor.stripColor(destinationName));
			coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		// get item metadata object
		ItemMeta itemMeta = itemStack.getItemMeta();

		// set item metadata display name to value from config file
		//noinspection ConstantConditions
		itemMeta.setDisplayName(displayName);

		// set item metadata Lore to value from config file
		itemMeta.setLore(coloredLore);

		// set persistent data in item metadata
		itemMeta.getPersistentDataContainer()
				.set(PERSISTENT_KEY, PersistentDataType.STRING, Destination.deriveKey(destinationName));

		// save new item metadata
		itemStack.setItemMeta(itemMeta);
	}


	/**
	 * Check if itemStack is a LodeStar item
	 *
	 * @param itemStack the ItemStack to test if LodeStar item
	 * @return {@code true} if ItemStack is LodeStar item, {@code false} if it is not
	 */
	public static boolean isItem(final ItemStack itemStack) {

		// if item stack is empty (null or air) return false
		if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
			return false;
		}

		// if item stack does not have metadata return false
		if (!itemStack.hasItemMeta()) {
			return false;
		}

		// if item stack has persistent data tag, return true; otherwise return false
		//noinspection ConstantConditions
		return itemStack.getItemMeta().getPersistentDataContainer()
				.has(PERSISTENT_KEY, PersistentDataType.STRING);
	}


	/**
	 * Get display name for destination associated with item
	 *
	 * @param itemStack the item whose destination name is being retrieved
	 * @return String - destination display name
	 */
	public static String getName(final ItemStack itemStack) {

		String key = LodeStar.getKey(itemStack);

		// if key is null, return empty string
		if (key == null) {
			return "";
		}

		String destinationName = null;

		// if destination is spawn get spawn display name from messages files
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

		// if no destination name found, use key for name
		if (destinationName == null) {
			destinationName = key;
		}

		return destinationName;
	}


	/**
	 * Get destination key encoded in item persistent meta data
	 *
	 * @param itemStack the item stack from which to retrieve stored key
	 * @return String - destination key, or null if item does not have key in persistent meta data
	 */
	public static String getKey(final ItemStack itemStack) {

		// if item stack does not have metadata, return null
		if (!itemStack.hasItemMeta()) {
			return null;
		}

		// if item stack does not have key in persistent data, return null
		//noinspection ConstantConditions
		if (!itemStack.getItemMeta().getPersistentDataContainer().has(PERSISTENT_KEY, PersistentDataType.STRING)) {
			return null;
		}

		// return key stored in item stack persistent data
		return itemStack.getItemMeta().getPersistentDataContainer().get(PERSISTENT_KEY, PersistentDataType.STRING);
	}


	/**
	 * Check if itemStack is a default LodeStar item
	 *
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
		String defaultMaterialString = plugin.getConfig().getString("default-material");

		if (defaultMaterialString == null) {
			defaultMaterialString = "NETHER_STAR";
		}

		Material material = Material.matchMaterial(defaultMaterialString);

		// if no match set to nether star
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// if material and data match defaults return true
		return itemStack.getType().equals(material);
	}

}
