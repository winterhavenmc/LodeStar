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

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;

import org.bukkit.entity.Player;


/**
 * Class that manages player teleportation, including warmup and cooldown.
 */
public final class TeleportHandler {

	// reference to main class
	private final PluginMain plugin;

	// map containing player UUID as key and warmup task id as value
	private final WarmupMap warmupMap;

	// map to store player UUID and cooldown expire time in milliseconds
	private final CooldownMap cooldownMap;

	// teleport executor instance that serves all teleporters
	private final TeleportExecutor teleportExecutor;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public TeleportHandler(final PluginMain plugin) {
		this.plugin = plugin;
		this.warmupMap = new WarmupMap(plugin);
		this.cooldownMap = new CooldownMap(plugin);
		this.teleportExecutor = new TeleportExecutor(plugin, warmupMap);
	}


	/**
	 * Start the player teleport
	 *
	 * @param player the player being teleported
	 */
	public void initiateTeleport(final Player player) {

		// if player is warming up, do nothing and return
		if (warmupMap.isWarmingUp(player)) {
			return;
		}

		// if player cooldown has not expired, send player cooldown message and return
		if (cooldownMap.isCoolingDown(player)) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_COOLDOWN)
					.setMacro(Macro.DURATION, cooldownMap.getCooldownTimeRemaining(player))
					.send();
			return;
		}

		// get key from player item
		String key = plugin.lodeStarUtility.getKey(player.getInventory().getItemInMainHand());

		if (key == null) {
			return;
		}

		Teleporter teleporter;

		// if item key is home key, teleport to bed spawn location
		if (key.equals(plugin.messageBuilder.getHomeDisplayName().orElse("Home"))) {
			teleporter = new HomeTeleporter(plugin, teleportExecutor);
		}
		// if item key is spawn key, teleport to world spawn location
		else if (key.equals(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"))) {
			teleporter = new SpawnTeleporter(plugin, teleportExecutor);
		}
		// teleport to destination for key
		else {
			teleporter = new DestinationTeleporter(plugin, teleportExecutor);
		}

		// initiate teleport
		teleporter.initiate(player);
	}


	/**
	 * Cancel pending teleport for player
	 *
	 * @param player the player to cancel teleport
	 */
	public void cancelTeleport(final Player player) {

		// if player is in warmup hashmap, cancel delayed teleport task and remove player from warmup hashmap
		if (warmupMap.containsPlayer(player)) {

			// get delayed teleport task id
			int taskId = warmupMap.getTaskId(player);

			// cancel delayed teleport task
			plugin.getServer().getScheduler().cancelTask(taskId);

			// remove player from warmup hashmap
			warmupMap.removePlayer(player);
		}
	}


	/**
	 * Test if player uuid is in warmup hashmap. Public pass through method.
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	public boolean isWarmingUp(final Player player) {
		return warmupMap.isWarmingUp(player);
	}


	/**
	 * Remove player uuid from warmup hashmap. Public pass through method.
	 *
	 * @param player the player to remove from the warmup map
	 */
	public void removeWarmingUpPlayer(final Player player) {
		warmupMap.removePlayer(player);
	}


	/**
	 * Insert player into cooldown map
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void startPlayerCooldown(final Player player) {
		cooldownMap.startPlayerCooldown(player);
	}


	void cancelPlayerCooldown(final Player player) {
		cooldownMap.removePlayer(player);
	}


	boolean isCoolingDown(Player player) {
		return cooldownMap.isCoolingDown(player);

	}
}
