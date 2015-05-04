package com.winterhaven_mc.lodestar;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

/**
 * Implements warmup tasks for <code>LodeStar</code>.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
class WarmupManager {
	
	final LodeStarMain plugin;		// reference to main class
	private ConcurrentHashMap<UUID,Integer> warmupMap;

	
	/**
	 * constructor method for <code>WarmupManager</code> class
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
	WarmupManager(LodeStarMain plugin) {
		this.plugin = plugin;
		warmupMap = new ConcurrentHashMap<UUID,Integer>();
	}

	
	/**
	 * Insert player uuid and taskId into warmup hashmap.
	 * @param player
	 * @param taskId
	 */
	void putPlayer(final Player player, final Integer taskId) {
		warmupMap.put(player.getUniqueId(), taskId);
	}
	
	
	/**
	 * Remove player uuid from warmup hashmap.
	 * @param player
	 */
	void removePlayer(final Player player) {		
		warmupMap.remove(player.getUniqueId());
	}
	
	
	/**
	 * Test if player uuid is in warmup hashmap.
	 * @param player
	 * @return
	 */
	boolean isWarmingUp(final Player player) {
		
		// if player is in warmup hashmap, return true, otherwise return false
		if (warmupMap.containsKey(player.getUniqueId())) {
			return true;
		}
		return false;
	}
	
	
	void cancelTeleport(final Player player) {
		
		// if player is in warmup hashmap, cancel delayed teleport task and remove player from warmup hashmap
		if (warmupMap.containsKey(player.getUniqueId())) {
			
			// get delayed teleport task id
			int taskId = warmupMap.get(player.getUniqueId());

			// cancel delayed teleport task
			plugin.getServer().getScheduler().cancelTask(taskId);
			
			// remove player from warmup hashmap
			warmupMap.remove(player.getUniqueId());
			
		}
	}
	
}

