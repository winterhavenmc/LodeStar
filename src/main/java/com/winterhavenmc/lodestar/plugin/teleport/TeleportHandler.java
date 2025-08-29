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

package com.winterhavenmc.lodestar.plugin.teleport;

import com.winterhavenmc.lodestar.plugin.PluginMain;
import com.winterhavenmc.lodestar.plugin.messages.Macro;
import com.winterhavenmc.lodestar.plugin.messages.MessageId;
import org.bukkit.entity.Player;


/**
 * Class that manages player teleportation, including warmup and cooldown.
 */
public final class TeleportHandler
{
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
	public TeleportHandler(final PluginMain plugin)
	{
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
	public void initiateTeleport(final Player player)
	{
		// if player is warming up, do nothing and return
		if (isWarmingUp(player))
		{
			return;
		}

		// if player cooldown has not expired, send player cooldown message and return
		if (isCoolingDown(player))
		{
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_COOLDOWN)
					.setMacro(Macro.DURATION, cooldownMap.getCooldownTimeRemaining(player))
					.send();
			return;
		}

		// get key from player item
		final String key = plugin.lodeStarUtility.getKey(player.getInventory().getItemInMainHand());

		// if item key is null, do nothing and return
		if (key == null)
		{
			return;
		}

		// get appropriate teleporter type for destination
		Teleporter teleporter = switch (plugin.lodeStarUtility.getDestinationType(key))
		{
			case HOME -> new HomeTeleporter(plugin, teleportExecutor);
			case SPAWN -> new SpawnTeleporter(plugin, teleportExecutor);
			default -> new DestinationTeleporter(plugin, teleportExecutor);
		};

		// initiate teleport
		teleporter.initiate(player);
	}


	/**
	 * Cancel pending teleport for player
	 *
	 * @param player the player to cancel teleport
	 */
	public void cancelTeleport(final Player player)
	{
		// if player is in warmup hashmap, cancel delayed teleport task and remove player from warmup hashmap
		if (warmupMap.containsPlayer(player))
		{
			// get delayed teleport task id
			int taskId = warmupMap.getTaskId(player);

			// cancel delayed teleport task
			plugin.getServer().getScheduler().cancelTask(taskId);

			// remove player from warmup hashmap
			warmupMap.removePlayer(player);
		}
	}


	/**
	 * Test if player uuid is in warmup map
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	public boolean isWarmingUp(final Player player)
	{
		return warmupMap.isWarmingUp(player);
	}


	/**
	 * Remove player uuid from warmup map
	 *
	 * @param player the player to remove from the warmup map
	 */
	public void removeWarmingUpPlayer(final Player player)
	{
		warmupMap.removePlayer(player);
	}


	/**
	 * Insert player into cooldown map
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void startPlayerCooldown(final Player player)
	{
		cooldownMap.startPlayerCooldown(player);
	}


	/**
	 * Remove player from cooldown map
	 *
	 * @param player the player to be removed from the cooldown map
	 */
	public void cancelPlayerCooldown(final Player player)
	{
		cooldownMap.removePlayer(player);
	}


	/**
	 * Test if a player is currently in the cooldown map
	 *
	 * @param player the player to check
	 * @return true if player is currently in the cooldown map, false if not
	 */
	boolean isCoolingDown(final Player player)
	{
		return cooldownMap.isCoolingDown(player);
	}

}
