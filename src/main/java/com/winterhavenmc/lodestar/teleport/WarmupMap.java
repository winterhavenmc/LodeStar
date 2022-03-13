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

package com.winterhavenmc.lodestar.teleport;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class WarmupMap {

	// reference to plugin main class
	private final JavaPlugin plugin;

	// HashMap containing player UUID as key and warmup task id as value
	private final ConcurrentHashMap<UUID, Integer> warmupMap;

	// Map containing player uuid for teleport initiated
	private final Set<UUID> teleportInitiated;



	WarmupMap(final JavaPlugin plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize warmup HashMap
		warmupMap = new ConcurrentHashMap<>();

		// initialize teleport initiated set
		teleportInitiated = ConcurrentHashMap.newKeySet();
	}


	/**
	 * Remove player uuid from warmup hashmap.
	 *
	 * @param player the player to remove from the warmup map
	 */
	void removePlayer(final Player player) {
		warmupMap.remove(player.getUniqueId());
	}


	/**
	 * Test if player uuid is in warmup hashmap
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	boolean isWarmingUp(final Player player) {
		return warmupMap.containsKey(player.getUniqueId());
	}


	/**
	 * Insert player uuid and taskId into warmup hashmap.
	 *
	 * @param player the player to be inserted in the warmup map
	 * @param taskId the taskId of the player's delayed teleport task
	 */
	void startPlayerWarmUp(final Player player, final Integer taskId) {

		warmupMap.put(player.getUniqueId(), taskId);

		// insert player uuid into teleport initiated set
		teleportInitiated.add(player.getUniqueId());

		// create task to remove player uuid from tpi set after set amount of ticks (default: 2)
		new BukkitRunnable() {
			@Override
			public void run() {
				teleportInitiated.remove(player.getUniqueId());
			}
		}.runTaskLater(plugin, plugin.getConfig().getInt("interact-delay", 2));

	}


	boolean containsPlayer(final Player player) {
		return warmupMap.containsKey(player.getUniqueId());
	}


	int getTaskId(final Player player) {
		return warmupMap.get(player.getUniqueId());
	}


	/**
	 * Check if player is in teleport initiated set
	 *
	 * @param player the player to check if teleport is initiated
	 * @return {@code true} if teleport been initiated, {@code false} if it has not
	 */
	boolean isInitiated(final Player player) {

		// check for null parameter
		if (player == null) {
			return false;
		}

		return !teleportInitiated.contains(player.getUniqueId());
	}

}
