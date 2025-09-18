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

import com.winterhavenmc.lodestar.models.destination.*;
import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

import static com.winterhavenmc.library.time.TimeUnit.SECONDS;


class TeleportExecutor
{
	private final LodeStarPluginController.TeleporterContextContainer ctx;
	private final TeleportHandler teleportHandler;
	protected final WarmupMap warmupMap;


	TeleportExecutor(final TeleportHandler teleportHandler, final LodeStarPluginController.TeleporterContextContainer ctx, final WarmupMap warmupMap)
	{
		this.ctx = ctx;
		this.teleportHandler = teleportHandler;
		this.warmupMap = warmupMap;
	}


	/**
	 * Execute the teleport to validDestination
	 *
	 * @param player      the player to teleport
	 * @param validDestination the validDestination
	 * @param messageId   the teleport warmup message to send to player
	 */
	void execute(final Player player, final ValidDestination validDestination, final MessageId messageId)
	{
		Location location = switch (validDestination)
		{
			case HomeDestination ignored -> (player.getRespawnLocation() != null)
					? player.getRespawnLocation()
					: ctx.worldManager().getSpawnLocation(player.getWorld());
			case SpawnDestination ignored -> ctx.worldManager().getSpawnLocation(player.getWorld());
			case StoredDestination stored -> stored.getLocation();
			case TeleportDestination teleportDestination -> teleportDestination.getLocation();
		};

		if (location == null)
		{
			//TODO: ensure home and spawn locations have non-null location here
			ctx.plugin().getLogger().info("location was null in TeleportExecutor.execute() method");
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, validDestination)
					.send();
		}

		// if player is less than configured minimum distance from the destination location, send player proximity message and return
		else if (isUnderMinimumDistance(player, location))
		{
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_FAIL_PROXIMITY)
					.setMacro(Macro.ITEM, player.getInventory().getItemInMainHand())
					.setMacro(Macro.DESTINATION, validDestination)
					.send();
		}
		else
		{
			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			removeFromInventoryOnUse(player, player.getInventory().getItemInMainHand());

			// initiate delayed teleport for player to final validDestination
			BukkitTask teleportTask = new DelayedTeleportTask(teleportHandler, ctx, player, validDestination,
					location, player.getInventory().getItemInMainHand().clone())
					.runTaskLater(ctx.plugin(), SECONDS.toTicks(ctx.plugin().getConfig().getLong("teleport-warmup")));

			// if configured warmup time is greater than zero, send warmup message
			sendWarmupMessage(player, validDestination, messageId);

			// insert player and taskId into warmup hashmap
			warmupMap.startPlayerWarmUp(player, teleportTask.getTaskId());

			// load validDestination chunk if not already loaded
			loadDestinationChunk(location);

			// if log-use is enabled in config, write log entry
			logUsage(player, validDestination);
		}
	}


	private Location getHomeOrFallback(final Player player)
	{
		return (player.getRespawnLocation() != null)
				? player.getRespawnLocation()
				: ctx.worldManager().getSpawnLocation(player.getWorld());
	}


	/**
	 * Send teleport warmup message if warmup time is greater than zero
	 *
	 * @param player      the teleporting player
	 * @param validDestination the teleport validDestination
	 * @param messageId   the message identifier
	 */
	private void sendWarmupMessage(final Player player, final ValidDestination validDestination, final MessageId messageId)
	{
		// get configured warmup time
		Duration warmupTime = Duration.ofSeconds(ctx.plugin().getConfig().getLong("teleport-warmup"));

		// if warmup time is greater than zero, send player warmup message
		if (warmupTime.isPositive())
		{
			ctx.messageBuilder().compose(player, messageId)
					.setMacro(Macro.DESTINATION, validDestination)
					.setMacro(Macro.DURATION, warmupTime)
					.send();

			// if enabled, play teleport warmup sound effect
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_WARMUP);
		}
	}


	/**
	 * Preload chunk at teleport validDestination if not already loaded
	 *
	 * @param location the teleport location for which to load chunk
	 */
	private void loadDestinationChunk(final Location location)
	{
		if (location != null && location.getWorld() != null && !location.getWorld().getChunkAt(location).isLoaded())
		{
			location.getWorld().getChunkAt(location).load();
		}
	}


	/**
	 * Check if player is within configured minimum distance from validDestination
	 *
	 * @param player    the player
	 * @param location the destination location to check for minimum distance
	 * @return true if under minimum distance, false if not
	 */
	private boolean isUnderMinimumDistance(final Player player, final Location location)
	{
		return location != null && location.getWorld() != null
				&& player.getWorld().equals(location.getWorld())
				&& player.getLocation().distanceSquared(location) < Math.pow(ctx.plugin().getConfig().getInt("minimum-distance"), 2);
	}


	/**
	 * remove one lode star item from player inventory
	 *
	 * @param player     the player
	 * @param playerItem the item
	 */
	final void removeFromInventoryOnUse(final Player player, final ItemStack playerItem)
	{
		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		if ("on-use".equalsIgnoreCase(ctx.plugin().getConfig().getString("remove-from-inventory")))
		{
			playerItem.setAmount(playerItem.getAmount() - 1);
			player.getInventory().setItemInMainHand(playerItem);
		}
	}


	/**
	 * Log player usage of lodestar item
	 *
	 * @param player the player being logged
	 */
	private void logUsage(final Player player, final ValidDestination validDestination)
	{
		// if log-use is enabled in config, write log entry
		if (ctx.plugin().getConfig().getBoolean("log-use"))
		{
			// send message to console
			ctx.messageBuilder().compose(ctx.plugin().getServer().getConsoleSender(), MessageId.EVENT_ITEM_USE_LOG)
					.setMacro(Macro.PLAYER, player)
					.setMacro(Macro.ITEM, player.getInventory().getItemInMainHand())
					.setMacro(Macro.DESTINATION, validDestination)
					.send();
		}
	}

}
