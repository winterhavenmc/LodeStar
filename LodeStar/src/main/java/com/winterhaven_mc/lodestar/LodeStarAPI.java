package com.winterhaven_mc.lodestar;

import java.util.List;

import org.bukkit.entity.Player;

/**
 * Public API to access LodeStar attributes 
 * @author Tim Savage
 *
 */
public interface LodeStarAPI {

	/**
	 * Get ItemStack containing one LodeStar
	 * @return
	 */
//	ItemStack getItem();
	
	/**
	 * Get material type of LodeStar
	 * @return Material
	 */
//	Material getItemMaterial();
	
	/**
	 * Get material data of LodeStar
	 * @return
	 */
//	MaterialData getItemData();

	/**
	 * Get display name of LodeStar with formatting intact
	 * @return String
	 */
	String getItemName();
	
	/**
	 * Get lore of LodeStar with formatting intact
	 * @return List of Strings
	 */
//	List<String> getItemLore();
	
	/**
	 * check if LodeStar items are allowed in crafting recipes
	 * @return Boolean
	 */
	Boolean isValidIngredient();
	
	/**
	 * LodeStar teleport cooldown time in seconds
	 * @return int
	 */
	int getCooldownTime();
	
	/**
	 * LodeStar teleport warmup time in seconds
	 * @return int
	 */
	int getWarmupTime();
	
	/**
	 * Minimum distance from destination to use a LodeStar item
	 * @return int
	 */
	int getMinDistance();
	
	/**
	 * Is teleport cancellation on player damage during warmup configured
	 * @return Boolean
	 */
	Boolean isCancelledOnDamage();

	/**
	 * Is teleport cancellation on player movement during warmup configured
	 * @return Boolean
	 */
	Boolean isCancelledOnMovement();
	
	/**
	 * Is teleport cancellation on player interaction with blocks during warmup configured
	 * @return Boolean
	 */
	Boolean isCancelledOnInteraction();

	/**
	 * Is player teleportation pending
	 * @param player
	 * @return Boolean
	 */
	Boolean isWarmingUp(Player player);

	/**
	 * Is player teleport cooldown in effect for this player
	 * @param player
	 * @return Boolean
	 */
	Boolean isCoolingDown(Player player);

	/**
	 * Time remaining for player cooldown in seconds for this player
	 * @param player
	 * @return long
	 */
	long cooldownTimeRemaining(Player player);
	
	/**
	 * Get list of worlds in which the LodeStar plugin is enabled
	 * @return List of Strings
	 */
	List<String> getEnabledWorlds();
	
	/**
	 * Cancel pending teleport for this player
	 * @param player
	 */
	void cancelTeleport(Player player);

}
