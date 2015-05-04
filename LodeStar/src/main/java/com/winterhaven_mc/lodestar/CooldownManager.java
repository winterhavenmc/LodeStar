package com.winterhaven_mc.lodestar;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Implements cooldown tasks for <code>LodeStar</code>.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
class CooldownManager {
	
	private final LodeStarMain plugin;		// reference to main class
	private ConcurrentHashMap<UUID, Long> cooldown;	// private hashmap to store player uuids and cooldown expire times

	
	/**
	 * constructor method for <code>CooldownManager</code> class
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
CooldownManager(LodeStarMain plugin) {
		this.plugin = plugin;
		cooldown = new ConcurrentHashMap<UUID, Long>();
	}

	
	/**
	 * Insert player uuid into cooldown hashmap with <code>expiretime</code> as value.<br>
	 * Schedule task to remove player uuid from cooldown hashmap when time expires.
	 * @param player
	 */
	void setPlayerCooldown(final Player player) {

		int cooldown_seconds = plugin.getConfig().getInt("teleport-cooldown");

		Long expiretime = System.currentTimeMillis() + (cooldown_seconds * 1000);
		cooldown.put(player.getUniqueId(), expiretime);
		new BukkitRunnable(){

			public void run() {
				cooldown.remove(player.getUniqueId());
			}
		}.runTaskLater(plugin, (cooldown_seconds * 20));
	}
	
	
	/**
	 * Get time remaining for player cooldown
	 * @param player
	 * @return long remainingtime
	 */
	long getTimeRemaining(Player player) {
		long remainingtime = 0;
		if (cooldown.containsKey(player.getUniqueId())) {
			remainingtime = (cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
		}
		return remainingtime;
	}

}

