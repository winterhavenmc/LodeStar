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
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import static com.winterhavenmc.util.TimeUnit.SECONDS;


class TeleportExecutor
{
	protected final PluginMain plugin;
	protected final WarmupMap warmupMap;


	TeleportExecutor(final PluginMain plugin, final WarmupMap warmupMap)
	{
		this.plugin = plugin;
		this.warmupMap = warmupMap;
	}


	/**
	 * Execute the teleport to destination
	 *
	 * @param player      the player to teleport
	 * @param destination the destination
	 * @param messageId   the teleport warmup message to send to player
	 */
	void execute(final Player player, final Destination destination, final MessageId messageId)
	{
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if destination location is empty, send invalid destination message and return
		if (destination.getLocation().isEmpty())
		{
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
					.send();
			return;
		}

		// if player is less than configured minimum distance from destination, send player proximity message and return
		if (isUnderMinimumDistance(player, destination))
		{
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_PROXIMITY)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
					.send();
			return;
		}

		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		removeFromInventoryOnUse(player, playerItem);

		// initiate delayed teleport for player to final destination
		BukkitTask teleportTask = new DelayedTeleportTask(plugin, player, destination, playerItem.clone())
				.runTaskLater(plugin, SECONDS.toTicks(plugin.getConfig().getLong("teleport-warmup")));

		// if configured warmup time is greater than zero, send warmup message
		sendWarmupMessage(player, destination, messageId);

		// insert player and taskId into warmup hashmap
		warmupMap.startPlayerWarmUp(player, teleportTask.getTaskId());

		// load destination chunk if not already loaded
		loadDestinationChunk(destination);

		// if log-use is enabled in config, write log entry
		logUsage(player, destination);
	}


	/**
	 * Send teleport warmup message if warmup time is greater than zero
	 *
	 * @param player      the teleporting player
	 * @param destination the teleport destination
	 * @param messageId   the message identifier
	 */
	private void sendWarmupMessage(final Player player, final Destination destination, final MessageId messageId)
	{
		// get configured warmup time
		long warmupTime = plugin.getConfig().getLong("teleport-warmup");

		// if warmup time is greater than zero, send player warmup message
		if (warmupTime > 0)
		{
			plugin.messageBuilder.compose(player, messageId)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
//					.setMacro(Macro.DESTINATION_WORLD, plugin.getServer().getWorld(destination.getWorldUid()))
					.setMacro(Macro.DURATION, SECONDS.toMillis(warmupTime))
					.send();

			// if enabled, play teleport warmup sound effect
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_WARMUP);
		}
	}


	/**
	 * Preload chunk at teleport destination if not already loaded
	 *
	 * @param destination the destination location
	 */
	private void loadDestinationChunk(final Destination destination)
	{
		// if optional destination location is empty, do nothing and return
		if (destination.getLocation().isEmpty())
		{
			return;
		}

		// unwrap optional destination location
		Location location = destination.getLocation().get();

		if (location.getWorld() != null && !location.getWorld().getChunkAt(location).isLoaded())
		{
			location.getWorld().getChunkAt(location).load();
		}
	}


	/**
	 * Check if player is within configured minimum distance from destination
	 *
	 * @param player      the player
	 * @param destination the destination
	 * @return true if under minimum distance, false if not
	 */
	private boolean isUnderMinimumDistance(final Player player, final Destination destination)
	{
		// if destination location is empty, return false
		if (destination.getLocation().isEmpty())
		{
			return false;
		}

		// unwrap optional destination location
		Location location = destination.getLocation().get();

		// check if location is within minimum proximity to player
		return location.getWorld() != null
				&& player.getWorld().equals(location.getWorld())
				&& player.getLocation().distanceSquared(location) < Math.pow(plugin.getConfig().getInt("minimum-distance"), 2);
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
		if ("on-use".equalsIgnoreCase(plugin.getConfig().getString("remove-from-inventory")))
		{
			playerItem.setAmount(playerItem.getAmount() - 1);
			player.getInventory().setItemInMainHand(playerItem);
		}
	}


	/**
	 * Log player usage of homestar item
	 *
	 * @param player the player being logged
	 */
	private void logUsage(final Player player, final Destination destination)
	{
		// if log-use is enabled in config, write log entry
		if (plugin.getConfig().getBoolean("log-use"))
		{
			// send message to console
			plugin.messageBuilder.compose(plugin.getServer().getConsoleSender(), MessageId.TELEPORT_LOG_USAGE)
					.setMacro(Macro.TARGET_PLAYER, player)
					.setMacro(Macro.DESTINATION, destination)
					.send();
		}
	}

}
