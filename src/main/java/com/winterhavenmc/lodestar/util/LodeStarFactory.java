package com.winterhavenmc.lodestar.util;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.storage.Destination;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;


/**
 * Utility class with static methods for creating and using LodeStar item stacks
 */
@SuppressWarnings("FieldCanBeLocal")
public final class LodeStarFactory {

	// static reference to main class instance
	private final PluginMain plugin;

	// name spaced key for persistent data
	public final NamespacedKey PERSISTENT_KEY;

	// item metadata fields
	private final Material defaultMaterial = Material.NETHER_STAR;
	private final Material material;
	private final int quantity;
	private final String itemStackName;
	private final List<String> itemStackLore;

	// item metadata flags
	private static final Set<ItemFlag> itemFlagSet = Set.of(
			ItemFlag.HIDE_ATTRIBUTES,
			ItemFlag.HIDE_ENCHANTS,
			ItemFlag.HIDE_UNBREAKABLE );

	// the proto item
	private final ItemStack protoItem;


	/**
	 * class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public LodeStarFactory(final PluginMain plugin) {

		this.plugin = plugin;

		PERSISTENT_KEY = new NamespacedKey(plugin, "destination");

		this.quantity = 1;
		this.itemStackName = plugin.messageBuilder.getItemName();
		this.itemStackLore = plugin.messageBuilder.getItemLore();

		// get default material string from configuration file
		String configMaterialString = plugin.getConfig().getString("item-material");

		// if config material string is null, set material to default material
		if (configMaterialString == null) {
			material = defaultMaterial;
		} else {
			// try to match material
			Material matchedMaterial = Material.matchMaterial(configMaterialString);

			// if no match or unobtainable item material, set material to default material
			if (matchedMaterial == null || !matchedMaterial.isItem()) {
				material = defaultMaterial;
			} else {
				// set material to matched material
				material = matchedMaterial;
			}
		}

		// assign new item stack of specified material and quantity to proto item
		this.protoItem = new ItemStack(material, quantity);

		// get item metadata for proto item
		final ItemMeta itemMeta = protoItem.getItemMeta();

		// set item metadata display name to value from language file
		//noinspection ConstantConditions
		itemMeta.setDisplayName(itemStackName);

		// set item metadata Lore to value from language file
		itemMeta.setLore(itemStackLore);

		// set persistent data in item metadata
		itemMeta.getPersistentDataContainer().set(PERSISTENT_KEY, PersistentDataType.STRING, "");

		// set metadata flags in item metadata
		for (ItemFlag itemFlag : itemFlagSet) {
			itemMeta.addItemFlags(itemFlag);
		}

		// save new proto item metadata
		protoItem.setItemMeta(itemMeta);
	}


	/**
	 * Create an item stack with encoded destination
	 *
	 * @param destinationName the destination name
	 * @return ItemStack with destination name and quantity
	 */
	public ItemStack create(final String destinationName) {
		return create(destinationName, 1);
	}


	/**
	 * Create an item stack with encoded destination
	 *
	 * @param destinationName the destination name
	 * @param quantity the number of items in the stack
	 * @return ItemStack with destination name and quantity
	 */
	public ItemStack create(final String destinationName, final int quantity) {

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
	public void setMetaData(final ItemStack itemStack, final String destinationName) {

		// retrieve item name from language file
		String itemName = plugin.messageBuilder.getInventoryItemName();

		// replace destination placeholder with destination name
		itemName = itemName.replace("%DESTINATION%", destinationName);

		// translate color codes
		itemName = ChatColor.translateAlternateColorCodes('&', itemName);

		// retrieve item lore from language file
		List<String> configLore = plugin.messageBuilder.getItemLore();

		// list of strings for formatted item lore
		List<String> itemLore = new ArrayList<>();

		// iterate over lines of lore and translate color codes and replace destination placeholder
		for (String line : configLore) {
			line = line.replace("%DESTINATION%", destinationName);
			line = ChatColor.translateAlternateColorCodes('&', line);
			itemLore.add(line);
		}

		// get item metadata object
		ItemMeta itemMeta = itemStack.getItemMeta();

		// set item metadata display name to value from config file
		//noinspection ConstantConditions
		itemMeta.setDisplayName(itemName);

		// set item metadata Lore to value from config file
		itemMeta.setLore(itemLore);

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
	public boolean isItem(final ItemStack itemStack) {

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
		return itemStack.getItemMeta().getPersistentDataContainer().has(PERSISTENT_KEY, PersistentDataType.STRING);
	}


	/**
	 * Get display name for destination associated with item
	 *
	 * @param itemStack the item whose destination name is being retrieved
	 * @return String - destination display name
	 */
	public String getDestinationName(final ItemStack itemStack) {

		String key = this.getKey(itemStack);

		// if key is null, return empty string
		if (key == null) {
			return "";
		}

		String destinationName = null;

		// if destination is spawn get spawn display name from messages files
		if (key.equals("spawn") || key.equals(Destination.deriveKey(plugin.messageBuilder.getSpawnDisplayName()))) {
			destinationName = plugin.messageBuilder.getSpawnDisplayName();
		}
		// if destination is home get home display name from messages file
		else if (key.equals("home")
				|| key.equals(Destination.deriveKey(plugin.messageBuilder.getHomeDisplayName()))) {
			destinationName = plugin.messageBuilder.getHomeDisplayName();
		}
		// else get destination name from datastore
		else {
			Destination destination = plugin.dataStore.selectRecord(key);
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
	public String getKey(final ItemStack itemStack) {

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
	public boolean isDefaultItem(final ItemStack itemStack) {

		if (plugin.getConfig().getBoolean("debug")) {
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


	/**
	 * Reload plugin's LodeStarFactory. Replaces existing plugin.LodeStarFactory with new instance.
	 */
	public void reload() {
		plugin.lodeStarFactory = new LodeStarFactory(plugin);
		plugin.getLogger().info("SpawnStarFactory reloaded.");
	}

}
