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
	ItemStack createItem(final String destinationName, final int qty) {

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
	 * @param itemStack
	 * @param formattedName
	 */
	void setMetaData(final ItemStack itemStack, final String destinationName) {
		
		// get key from destination name parameter
		String key = Destination.deriveKey(destinationName);
		
		// get formatted destination name
		String formattedName = getDestinationName(key);
		
		// retrieve item name and lore from language file file
		String displayName = plugin.messageManager.getInventoryItemName();
		List<String> configLore = plugin.messageManager.getItemLore();

		// substitute %destination% variable in display name
		displayName = displayName.replaceAll("%destination%", formattedName);
		displayName = displayName.replaceAll("%DESTINATION%", ChatColor.stripColor(formattedName));

		// allow for '&' character for color codes in name and lore
		displayName = ChatColor.translateAlternateColorCodes('&', displayName);

		ArrayList<String> coloredLore = new ArrayList<String>();
		
		for (String line : configLore) {
			line = line.replaceAll("%destination%", formattedName);
			line = line.replaceAll("%DESTINATION%", ChatColor.stripColor(formattedName));
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
	 * Check if itemStack is a LodeStar item
	 * @param itemStack
	 * @return boolean
	 */
	boolean isLodeStar(final ItemStack itemStack) {
		
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
	 * Check if itemStack is a LodeStar item
	 * @param itemStack
	 * @return boolean
	 */
	@SuppressWarnings("deprecation")
	boolean isDefaultItem(final ItemStack itemStack) {
		
		if (plugin.debug) {
			plugin.getLogger().info("isDefaultItem: " + itemStack.toString());
		}
		
		// if item stack is empty (null or air) return false
		if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
			return false;
		}

		Material material = null;
		byte materialDataByte = 0;
		
		// try to match default material from config
		String[] materialElements = plugin.getConfig().getString("default-material").split("\\s*:\\s*");

		// try to match material
		if (materialElements.length > 0) {
			material = Material.matchMaterial(materialElements[0]);
		}

		// try to match material data
		if (materialElements.length > 1) {
			try {
				materialDataByte = Byte.parseByte(materialElements[1]);
			}
			catch (NumberFormatException e2) {
				materialDataByte = (byte) 0;
			}
		}

		// if no match set to nether star
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// if material and data match defaults return true
		if (itemStack.getType().equals(material) && itemStack.getData().getData() == materialDataByte) {
			return true;
		}
		return false;
	}

	
	/**
	 * Gets destination key from itemStack
	 * @param itemStack
	 * @return destination key or null if item has no key
	 */
	String getKey(final ItemStack itemStack) {
		
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
	boolean isValidDestination(final String destinationName) {
		
		if (destinationName == null || destinationName.isEmpty()) {
			return false;
		}
		
		String key = Destination.deriveKey(destinationName);
		
		if (isReservedName(destinationName)	|| plugin.dataStore.getRecord(key) != null) {
			return true;
		}
		return false;
	}

	
	/**
	 * Check if destination is an allowable string
	 * @param destinationName
	 * @return
	 */
	boolean isAllowedName(final String destinationName) {
		
		// destination name cannot be null
		// destination name cannot be empty
		// destination name cannot start with a digit
		// destination name cannot contain a colon
		
		if (destinationName == null 
				|| destinationName.isEmpty()
				|| destinationName.matches("^\\d.*")
				|| destinationName.matches(".*:.*")) {
			return false;
		}
		return true;
	}

	
	/**
	 * Check if destination name is a reserved name
	 * @param destinationName
	 * @return boolean
	 */
	boolean isReservedName(final String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		if (isSpawnName(key) || isHomeName(key)) {
			return true;
		}
		return false;
	}

	
	boolean isHomeName(final String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		if (key.equals("home")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			return true;
		}
		return false;
	}

	
	boolean isSpawnName(final String destinationName) {
		
		String key = Destination.deriveKey(destinationName);
		if (key.equals("spawn") 
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			return true;
		}
		return false;
	}

	
	
	String hiddenString(final String s) {
		String hidden = "";
		for (char c : s.toCharArray())
			hidden += ChatColor.COLOR_CHAR + "" + c;
		return hidden;
	}
	

	@Override
	public String getItemName() {
		return plugin.messageManager.getItemName();
	}
	
	
	public String getDestinationName(final String key) {

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
	
	public String getDestinationName(final ItemStack itemStack) {
		
		String key = getKey(itemStack);
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
	public int getMinDistance() {
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
	public Boolean isWarmingUp(final Player player) {
		return plugin.warmupManager.isWarmingUp(player);
	}
	
	@Override
	public Boolean isCoolingDown(final Player player) {
		return plugin.cooldownManager.getTimeRemaining(player) > 0;
	}
	
	@Override
	public long cooldownTimeRemaining(final Player player) {
		return plugin.cooldownManager.getTimeRemaining(player);
	}
	
	@Override
	public List<String> getEnabledWorlds() {
		return plugin.commandManager.getEnabledWorlds();
	}
	
	@Override
	public void cancelTeleport(final Player player) {
		plugin.warmupManager.cancelTeleport(player);
	}

	
	/**
	 * Create an itemStack with default material and data from config
	 * @return ItemStack
	 */
	@Override
	public ItemStack getDefaultItem() {
		
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
		ItemStack newItem = new ItemStack(configMaterial,1,configMaterialDataByte);
		
		return newItem;
	}

}

