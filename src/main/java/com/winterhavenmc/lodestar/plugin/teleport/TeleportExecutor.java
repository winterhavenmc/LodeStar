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
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;
import com.winterhavenmc.lodestar.plugin.models.destination.ValidDestination;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import static com.winterhavenmc.library.TimeUnit.SECONDS;


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
	 * Execute the teleport to validDestination
	 *
	 * @param player      the player to teleport
	 * @param validDestination the validDestination
	 * @param messageId   the teleport warmup message to send to player
	 */
	void execute(final Player player, final ValidDestination validDestination, final MessageId messageId)
	{
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if validDestination location is empty, send invalid validDestination message and return
		if (validDestination.location() == null)
		{
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, validDestination.displayName())
					.setMacro(Macro.DESTINATION_LOCATION, validDestination.location())
					.send();
			return;
		}

		// if player is less than configured minimum distance from validDestination, send player proximity message and return
		if (isUnderMinimumDistance(player, validDestination))
		{
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_PROXIMITY)
					.setMacro(Macro.DESTINATION, validDestination.displayName())
					.setMacro(Macro.DESTINATION_LOCATION, validDestination.location())
					.send();
			return;
		}

		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		removeFromInventoryOnUse(player, playerItem);

		// initiate delayed teleport for player to final validDestination
		BukkitTask teleportTask = new DelayedTeleportTask(plugin, player, validDestination, playerItem.clone())
				.runTaskLater(plugin, SECONDS.toTicks(plugin.getConfig().getLong("teleport-warmup")));

		// if configured warmup time is greater than zero, send warmup message
		sendWarmupMessage(player, validDestination, messageId);

		// insert player and taskId into warmup hashmap
		warmupMap.startPlayerWarmUp(player, teleportTask.getTaskId());

		// load validDestination chunk if not already loaded
		loadDestinationChunk(validDestination);

		// if log-use is enabled in config, write log entry
		logUsage(player, validDestination);
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
		long warmupTime = plugin.getConfig().getLong("teleport-warmup");

		// if warmup time is greater than zero, send player warmup message
		if (warmupTime > 0)
		{
			plugin.messageBuilder.compose(player, messageId)
					.setMacro(Macro.DESTINATION, validDestination.displayName())
					.setMacro(Macro.DESTINATION_WORLD, validDestination.location().worldName())
					.setMacro(Macro.DURATION, SECONDS.toMillis(warmupTime))
					.send();

			// if enabled, play teleport warmup sound effect
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_WARMUP);
		}
	}


	/**
	 * Preload chunk at teleport validDestination if not already loaded
	 *
	 * @param validDestination the validDestination location
	 */
	private void loadDestinationChunk(final ValidDestination validDestination)
	{
		Location location = validDestination.location().toBukkitLocation();

		if (location != null && location.getWorld() != null && !location.getWorld().getChunkAt(location).isLoaded())
		{
			location.getWorld().getChunkAt(location).load();
		}
	}


	/**
	 * Check if player is within configured minimum distance from validDestination
	 *
	 * @param player      the player
	 * @param validDestination the validDestination
	 * @return true if under minimum distance, false if not
	 */
	private boolean isUnderMinimumDistance(final Player player, final ValidDestination validDestination)
	{
		Location location = validDestination.location().toBukkitLocation();

		return location != null && location.getWorld() != null
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
	private void logUsage(final Player player, final ValidDestination validDestination)
	{
		// if log-use is enabled in config, write log entry
		if (plugin.getConfig().getBoolean("log-use"))
		{
			// send message to console
			plugin.messageBuilder.compose(plugin.getServer().getConsoleSender(), MessageId.TELEPORT_LOG_USAGE)
					.setMacro(Macro.TARGET_PLAYER, player)
					.setMacro(Macro.DESTINATION, validDestination.displayName())
					.setMacro(Macro.DESTINATION_WORLD, plugin.worldManager.getAliasOrName(validDestination.location().worldName()))
					.send();
		}
	}

}
