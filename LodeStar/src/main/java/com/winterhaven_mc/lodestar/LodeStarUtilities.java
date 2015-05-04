package com.winterhaven_mc.lodestar;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Implements LodeStarAPI.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class LodeStarUtilities implements LodeStarAPI {

	private final LodeStarMain plugin;
	private final String itemTag = hiddenString("SSv2");
	
	LodeStarUtilities(LodeStarMain plugin) {
		this.plugin = plugin;
	}

	
	/**
	 * Create an item stack with encoded destination and quantity
	 * @param destinationName
	 * @param quantity
	 * @return
	 */
	ItemStack createItem(String destinationName, int quantity) {
		
		quantity = Math.max(quantity, 1);
		
		// get material type and data from config file
		String[] configMaterialElements = plugin.getConfig().getString("default-material").split("\\s*:\\s*");
		
		// try to match material
		Material configMaterial = Material.matchMaterial(configMaterialElements[0]);
		
		// if no match default to nether star
		if (configMaterial == null) {
			configMaterial = Material.NETHER_STAR;
		}
		
		// parse material data from config file if present
		byte configMaterialDataByte;
		
		// if data set in config try to parse as byte; set to zero if it doesn't parse
		if (configMaterialElements.length > 1) {
			try {
				configMaterialDataByte = Byte.parseByte(configMaterialElements[1]);
			}
			catch (NumberFormatException e) {
				configMaterialDataByte = (byte) 0;
			}
		}
		// if no data set in config default to zero
		else {
			configMaterialDataByte = (byte) 0;
		}
		
		// create item stack with configured material and data
		ItemStack newItem = new ItemStack(configMaterial,quantity,configMaterialDataByte);
		
		// set item display name and lore
		setMetaData(newItem, destinationName);
		
		// return new item
		return newItem;
	}
	
	
	/**
	 * Encode hidden destination key in item lore
	 * @param itemStack
	 * @param destinationName
	 */
	void setMetaData(ItemStack itemStack, String destinationName) {
		
		// get key from destination name parameter
		String key = Destination.deriveKey(destinationName);
		
		// if key equals 'spawn' set destinationName to spawn name from language file
		if (key.equals("spawn") 
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			destinationName = plugin.messageManager.getSpawnDisplayName();
		}
		else if (key.equalsIgnoreCase("home")
				|| key.equalsIgnoreCase(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			destinationName = plugin.messageManager.getSpawnDisplayName();
		}
		
		// retrieve item name and lore from language file file
		String displayName = plugin.messageManager.getInventoryItemName();
		List<String> configLore = plugin.messageManager.getItemLore();

		// substitute %destination% variable in display name
		displayName = displayName.replaceAll("%destination%", destinationName);
		displayName = displayName.replaceAll("%DESTINATION%", ChatColor.stripColor(destinationName));

		// allow for '&' character for color codes in name and lore
		displayName = ChatColor.translateAlternateColorCodes('&', displayName);

		ArrayList<String> coloredLore = new ArrayList<String>();
		
		for (String line : configLore) {
			line = line.replaceAll("%destination%", destinationName);
			line = line.replaceAll("%DESTINATION%", ChatColor.stripColor(destinationName));
			coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		String hiddenDestination = hiddenString(key);
		String hiddenSeparator = hiddenString("|");
		
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
	 * Check if itemStack is a spawn star item
	 * @param itemStack
	 * @return boolean
	 */
	boolean isLodeStar(ItemStack itemStack) {
		
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
		if (!itemLore.isEmpty() && itemLore.get(0).startsWith(itemTag)) {
			return true;
		}
		return false;
	}

	
	/**
	 * Gets destination key from itemStack
	 * @param itemStack
	 * @return destination key or null if item has no key
	 */
	String getKey(ItemStack itemStack) {
		
		String destinationKey = null;
		
		// check that item has lore
		if (!itemStack.getItemMeta().hasLore()) {
			return destinationKey;
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
	 * @param destinationName
	 * @return
	 */
	boolean isValidDestination(String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		
		if (key.equals("spawn")
				|| key.equals("home")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))
				|| plugin.dataStore.getRecord(key) != null) {
			return true;
		}
		return false;
	}

	
	/**
	 * Check if destination name is a reserved name
	 * @param destinationName
	 * @return
	 */
	boolean nameReserved(String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		if (key.equals("spawn") 
				|| key.equals("home")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			return true;
		}
		return false;
	}

	String hiddenString(String s) {
		String hidden = "";
		for (char c : s.toCharArray())
			hidden += ChatColor.COLOR_CHAR + "" + c;
		return hidden;
	}
	

	@Override
	public String getItemName() {
		return plugin.messageManager.getItemName();
	}
	
	public String getDestinationName(String key) {
		
		if (plugin.debug) {
			plugin.getLogger().info("[getDestinationName] key: " + key);
		}
		
		key = Destination.deriveKey(key);
		String destinationName = null;
		
		if (key.equals("spawn") || key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			destinationName = plugin.messageManager.getSpawnDisplayName();
		}
		else if (key.equals("home") 
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			destinationName = plugin.messageManager.getHomeDisplayName();
		}
		else {
			Destination destination = plugin.dataStore.getRecord(key);
			if (destination != null) {
				destinationName = destination.getDisplayName();
			}
		}
		
		if (destinationName == null) {
			destinationName = key;
		}
		
		if (plugin.debug) {
			plugin.getLogger().info("[getDestinationName] name: " + destinationName);
		}

		return destinationName;
	}
	
	@Override
	public Boolean isValidIngredient() {
		return plugin.getConfig().getBoolean("allow-in-recipes");
	}
	
	@Override
	public int getCooldownTime() {
		return plugin.getConfig().getInt("cooldown-time");
	}

	@Override
	public int getWarmupTime() {
		return plugin.getConfig().getInt("warmup-time");
	}


	@Override
	public int getMinSpawnDistance() {
		return plugin.getConfig().getInt("minimum-distance");
	}


	@Override
	public Boolean isCancelledOnDamage() {
		return plugin.getConfig().getBoolean("cancel-on-damage");
	}


	@Override
	public Boolean isCancelledOnMovement() {
		return plugin.getConfig().getBoolean("cancel-on-movement");
	}


	@Override
	public Boolean isCancelledOnInteraction() {
		return plugin.getConfig().getBoolean("cancel-on-interaction");
	}
	
	@Override
	public Boolean isWarmingUp(Player player) {
		return plugin.warmupManager.isWarmingUp(player);
	}
	
	@Override
	public Boolean isCoolingDown(Player player) {
		return plugin.cooldownManager.getTimeRemaining(player) > 0;
	}
	
	@Override
	public long cooldownTimeRemaining(Player player) {
		return plugin.cooldownManager.getTimeRemaining(player);
	}
	
	@Override
	public List<String> getEnabledWorlds() {
		return plugin.commandManager.getEnabledWorlds();
	}
	
	@Override
	public void cancelTeleport(Player player) {
		plugin.warmupManager.cancelTeleport(player);
	}

}

