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

package com.winterhavenmc.lodestar.plugin.teleport;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import org.bukkit.entity.Player;


/**
 * Class that manages player teleportation, including warmup and cooldown.
 */
public final class TeleportHandler
{
	private final WarmupMap warmupMap;
	private final CooldownMap cooldownMap;
	private final TeleportExecutor teleportExecutor;
	private final MessageBuilder<MessageId, Macro> messageBuilder;
	private final LodeStarUtility lodeStarUtility;
	private final PluginController.ContextContainer ctx;


	/**
	 * Class constructor
	 */
	public TeleportHandler(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.warmupMap = new WarmupMap();
		this.cooldownMap = new CooldownMap(this, ctx);
		this.teleportExecutor = new TeleportExecutor(this, ctx, warmupMap);
		this.messageBuilder = ctx.messageBuilder();
		this.lodeStarUtility = ctx.lodeStarUtility();
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
			messageBuilder.compose(player, MessageId.TELEPORT_COOLDOWN)
					.setMacro(Macro.DURATION, cooldownMap.getCooldownTimeRemaining(player))
					.send();
			return;
		}

		// get key from player item
		final String key = lodeStarUtility.getKey(player.getInventory().getItemInMainHand());

		// if item key is null, do nothing and return
		if (key == null)
		{
			return;
		}

		// get appropriate teleporter type for destination
		Teleporter teleporter = switch (lodeStarUtility.getDestinationType(key))
		{
			case HOME -> new HomeTeleporter(ctx, teleportExecutor);
			case SPAWN -> new SpawnTeleporter(ctx, teleportExecutor);
			default -> new DestinationTeleporter(ctx, teleportExecutor);
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
			ctx.plugin().getServer().getScheduler().cancelTask(taskId);

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
