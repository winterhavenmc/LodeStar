/*
 * Copyright (c) 2022-2025 Tim Savage.
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

package com.winterhavenmc.lodestar.plugin.util;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.messagebuilder.keys.ConstantKey;
import com.winterhavenmc.library.messagebuilder.keys.ItemKey;
import com.winterhavenmc.library.messagebuilder.keys.ValidConstantKey;
import com.winterhavenmc.library.messagebuilder.keys.ValidItemKey;
import com.winterhavenmc.library.messagebuilder.model.language.InvalidItemRecord;
import com.winterhavenmc.library.messagebuilder.model.language.InvalidRecordReason;
import com.winterhavenmc.library.messagebuilder.model.language.ItemRecord;
import com.winterhavenmc.library.messagebuilder.model.language.ValidItemRecord;
import com.winterhavenmc.lodestar.models.destination.*;
import com.winterhavenmc.lodestar.plugin.storage.DataStore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;





/**
 * Utility class for creating and testing SpawnStar item stacks
 * <p>
 * A loadstar item has two persistent data objects:
 *     1) namespaced key "ITEM_KEY" contains the item key string, corresponding to the item definition in the language file
 *     2) namespaced key "DESTINATION" contains the item destination name, whether spawn, home or stored destination
 */
public final class LodeStarUtility
{
	public static final ValidConstantKey HOME_KEY = ConstantKey.of("LOCATION.HOME").isValid().orElseThrow();
	public static final ValidConstantKey SPAWN_KEY = ConstantKey.of("LOCATION.SPAWN").isValid().orElseThrow();
	private final Plugin plugin;
	private final MessageBuilder messageBuilder;
	private final NamespacedKey ITEM_KEY;
	private final NamespacedKey DESTINATION_KEY;
	private final DataStore datastore;


	/**
	 * Class constructor
	 *
	 * @param plugin instance of plugin main class
	 * @param messageBuilder instance of message builder library
	 * @param datastore instance of datastore
	 */
	public LodeStarUtility(final Plugin plugin, final MessageBuilder messageBuilder, final DataStore datastore)
	{
		this.plugin = plugin;
		this.messageBuilder = messageBuilder;
		this.datastore = datastore;
		this.ITEM_KEY = new NamespacedKey(plugin, "LODESTAR");
		this.DESTINATION_KEY = new NamespacedKey(plugin, "DESTINATION");
	}


	/**
	 * Create a SpawnStar item stack of given quantity, with custom display name and lore
	 *
	 * @param passedQuantity number of SpawnStar items in newly created stack
	 * @return ItemStack of SpawnStar items
	 */
	public ItemStack create(final int passedQuantity, final String destinationName)
	{
		int quantity = Math.max(1, passedQuantity);
		ValidItemKey validItemKey = ItemKey.of("LODESTAR").isValid().orElseThrow();
		Map<String, String> replacements = Collections.singletonMap("DESTINATION", destinationName);

		Optional<ItemStack> itemStack = messageBuilder.itemForge().createItem(validItemKey, quantity, replacements);
		if (itemStack.isPresent())
		{
			ItemStack returnItem = itemStack.get();
			setPersistentDestination(returnItem, destinationName);
			return returnItem;
		}
		else
		{
			return null;
		}
	}


	public void setPersistentDestination(final ItemStack itemStack, final String destinationName)
	{
		if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null)
		{
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.getPersistentDataContainer().set(DESTINATION_KEY, PersistentDataType.STRING, destinationName);
			itemStack.setItemMeta(itemMeta);
		}
	}


	/**
	 * Get display name from key
	 *
	 * @param key the destination key for which to retrive the display name
	 * @return the destination display name
	 */
	public Optional<String> getDisplayName(final String key)
	{
		if (key == null) return Optional.empty();
		else if (key.equalsIgnoreCase(homeDisplayName())) return Optional.of(homeDisplayName());
		else if (key.equalsIgnoreCase(spawnDisplayName())) return Optional.of(spawnDisplayName());
		else if (datastore.destinations().get(key) instanceof ValidDestination validDestination) return Optional.of(validDestination.displayName());
		else return Optional.empty();
	}


	public String homeDisplayName()
	{
		return messageBuilder.constants().getString(LodeStarUtility.HOME_KEY).orElse("Home");
	}


	public String spawnDisplayName()
	{
		return messageBuilder.constants().getString(LodeStarUtility.SPAWN_KEY).orElse("Spawn");
	}


	public boolean isRerservedName(final String destinationName)
	{
		return destinationName.equalsIgnoreCase(messageBuilder.constants().getString(LodeStarUtility.SPAWN_KEY).orElse("spawn"))
				|| destinationName.equalsIgnoreCase(messageBuilder.constants().getString(LodeStarUtility.HOME_KEY).orElse("home"))
				|| destinationName.equalsIgnoreCase("spawn")
				|| destinationName.equalsIgnoreCase("home");
	}


	public boolean isHomeKey(final String key)
	{
		return key.equalsIgnoreCase(homeDisplayName()) || key.equalsIgnoreCase("home");
	}


	public boolean isSpawnKey(final String key)
	{
		return key.equalsIgnoreCase(spawnDisplayName()) || key.equalsIgnoreCase("spawn");
	}


	public boolean isDefaultMaterial(final ItemStack itemStack)
	{
		String defaultMaterialName = plugin.getConfig().getString("default-material");
		return defaultMaterialName != null && itemStack.getType().equals(Material.matchMaterial(defaultMaterialName));
	}


	public Destination getDestination(final String destinationName)
	{
		if (isHomeKey(destinationName)) return HomeDestination.of(messageBuilder.constants().getString(HOME_KEY).orElse("Home"));
		else if (isSpawnKey(destinationName)) return SpawnDestination.of(messageBuilder.constants().getString(SPAWN_KEY).orElse("SPAWN"));
		else return datastore.destinations().get(destinationName);
	}


	public Destination getDestination(final ItemStack itemStack)
	{
		if (itemStack == null) return new InvalidDestination("ø", "The itemStack parameter was null.");

		String destinationKey = getDestinationKey(itemStack);
		return getDestination(destinationKey);
	}


	/**
	 * Get destination key encoded in item persistent meta data
	 *
	 * @param itemStack the item stack from which to retrieve stored key
	 * @return String - destination key, or null if item does not have key in persistent metadata
	 */
	public String getDestinationKey(final ItemStack itemStack)
	{
		if (itemStack != null
				&& itemStack.hasItemMeta()
				&& itemStack.getItemMeta() != null
				&& itemStack.getItemMeta().getPersistentDataContainer().has(DESTINATION_KEY, PersistentDataType.STRING))
		{
			return itemStack.getItemMeta().getPersistentDataContainer().get(DESTINATION_KEY, PersistentDataType.STRING);
		}
		else
		{
			return null;
		}
	}


	public String deriveKey(final List<String> args)
	{
		// join remaining arguments with spaces
		String result = String.join(" ", args);

		// strip legacy color codes from entered name
		result = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', result));

		// perform any additional string sanitization here

		// return sanitized destination anme string
		return result;
	}


	public ItemRecord itemRecord(final String key)
	{
		ItemKey itemKey = ItemKey.of(key);
		if (itemKey instanceof ValidItemKey validItemKey)
		{
			return messageBuilder.items().getItemRecord(validItemKey);
		}
		else
		{
			return new InvalidItemRecord(itemKey, InvalidRecordReason.ITEM_KEY_INVALID);
		}
	}


	public void setItemMetadata(final ItemStack itemStack, final String destinationName)
	{
		// get LodeStar item record
		ItemRecord itemRecord = itemRecord("LODESTAR");

		if (itemRecord instanceof ValidItemRecord validItemRecord)
		{
			String inventoryName = validItemRecord.displayName().replace("{DESTINATION}", destinationName);
			List<String> lore = new ArrayList<>();
			for (String line : validItemRecord.lore())
			{
				lore.add(line.replace("{DESTINATION}", destinationName));
			}

			if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null)
			{
				ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setDisplayName(inventoryName);
				itemMeta.setLore(lore);
				itemMeta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "LODESTAR");
				itemMeta.getPersistentDataContainer().set(DESTINATION_KEY, PersistentDataType.STRING, destinationName);
				itemStack.setItemMeta(itemMeta);
			}
		}
	}

}
