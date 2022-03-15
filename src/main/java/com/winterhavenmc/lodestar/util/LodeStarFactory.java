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
 * Utility class with methods for creating and using LodeStar item stacks
 */
public final class LodeStarFactory {

	// static reference to main class instance
	private final PluginMain plugin;

	// name spaced key for persistent data
	public final NamespacedKey PERSISTENT_KEY;

	// item metadata flags
	private static final Set<ItemFlag> itemFlagSet = Set.of(
			ItemFlag.HIDE_ATTRIBUTES,
			ItemFlag.HIDE_ENCHANTS,
			ItemFlag.HIDE_UNBREAKABLE);


	/**
	 * class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public LodeStarFactory(final PluginMain plugin) {
		this.plugin = plugin;
		this.PERSISTENT_KEY = new NamespacedKey(plugin, "destination");
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
	 * @param quantity        the number of items in the stack
	 * @return ItemStack with destination name and quantity
	 */
	public ItemStack create(final String destinationName, final int quantity) {

		// clone proto item
		ItemStack newItem = getDefaultItemStack();

		// validate quantity is between 1 and max item stack size
		int newQuantity = Math.max(1, quantity);
		newQuantity = Math.min(newQuantity, newItem.getMaxStackSize());

		// if configured max give quantity is positive, validate quantity
		if (plugin.getConfig().getInt("max-give-amount") > 0) {
			newQuantity = Math.min(plugin.getConfig().getInt("max-give-amount"), newQuantity);
		}

		// set quantity
		newItem.setAmount(newQuantity);

		// set item meta data
		setMetaData(newItem, destinationName);

		// return new item
		return newItem;
	}


	/**
	 * Create an itemStack with default material and data from config
	 *
	 * @return ItemStack
	 */
	public ItemStack getDefaultItemStack() {

		// get configured material string
		String configMaterialString = plugin.getConfig().getString("default-material");

		// if string is null, use default
		if (configMaterialString == null) {
			configMaterialString = "NETHER_STAR";
		}

		// match material to configured string
		Material configMaterial = Material.matchMaterial(configMaterialString);

		// if no match or unobtainable material, default to nether star
		if (configMaterial == null || !configMaterial.isItem()) {
			configMaterial = Material.NETHER_STAR;
		}

		// return item stack of configured material
		return new ItemStack(configMaterial, 1);
	}


	/**
	 * Set meta data on item stack
	 *
	 * @param itemStack       the ItemStack to encode with destination key
	 * @param destinationName the destination name used to create the encoded key
	 */
	public void setMetaData(final ItemStack itemStack, final String destinationName) {

		// retrieve item name from language file
		String itemName = plugin.messageBuilder.getInventoryItemName().orElse("LodeStar: " + destinationName);

		// replace destination placeholder with destination name
		itemName = itemName.replace("%DESTINATION%", destinationName);

		// translate color codes
		itemName = ChatColor.translateAlternateColorCodes('&', itemName);

		// retrieve item lore from language file
		List<String> configLore = plugin.messageBuilder.getItemLore();

		// list of strings for formatted item lore
		List<String> itemLore = new LinkedList<>();

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
		itemMeta.getPersistentDataContainer().set(PERSISTENT_KEY, PersistentDataType.STRING, deriveKey(destinationName));

		// set item metadata flags
		for (ItemFlag itemFlag : itemFlagSet) {
			itemMeta.addItemFlags(itemFlag);
		}

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
		if (key.equals("spawn") || key.equals(deriveKey(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn")))) {
			destinationName = plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn");
		}
		// if destination is home, get home display name from messages file
		else if (key.equals("home") || key.equals(deriveKey(plugin.messageBuilder.getHomeDisplayName().orElse("Home")))) {
			destinationName = plugin.messageBuilder.getHomeDisplayName().orElse("Home");
		}
		// else get destination name from datastore
		else {
			Optional<Destination> optionalDestination = plugin.dataStore.selectRecord(key);
			if (optionalDestination.isPresent()) {
				destinationName = optionalDestination.get().getDisplayName();
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
	 * Derive key from destination display name<br>
	 * strips color codes and replaces spaces with underscores<br>
	 * if a destination key is passed, it will be returned unaltered
	 *
	 * @param destinationName the destination name to convert to a key
	 * @return String - the key derived from the destination name
	 */
	public String deriveKey(final String destinationName) {

		// validate parameter
		if (destinationName == null || destinationName.isBlank()) {
			return "";
		}

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

}
