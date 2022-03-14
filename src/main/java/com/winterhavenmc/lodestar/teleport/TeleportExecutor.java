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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import static com.winterhavenmc.util.TimeUnit.SECONDS;


class TeleportExecutor {

	protected final PluginMain plugin;
	protected final WarmupMap warmupMap;


	TeleportExecutor(final PluginMain plugin, final WarmupMap warmupMap) {
		this.plugin = plugin;
		this.warmupMap = warmupMap;
	}


	/**
	 * Execute the teleport to destination
	 *
	 * @param player      the player to teleport
	 * @param destination the destination
	 * @param playerItem  the LodeStar item used to initiate teleport
	 * @param messageId   the teleport warmup message to send to player
	 */
	void execute(final Player player, final Destination destination, final ItemStack playerItem, final MessageId messageId) {

		// if destination location is null, send invalid destination message and return
		if (destination.getLocation() == null) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
					.send();
			return;
		}

		// if player is less than configured minimum distance from destination, send player proximity message and return
		if (isUnderMinimumDistance(player, destination)) {
			plugin.messageBuilder.build(player, MessageId.TELEPORT_FAIL_PROXIMITY)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
					.send();
			return;
		}

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
	 * @param player the teleporting player
	 * @param destination the teleport destination
	 * @param messageId the message identifier
	 */
	private void sendWarmupMessage(Player player, Destination destination, MessageId messageId) {
		long warmupTime = plugin.getConfig().getLong("teleport-warmup");
		if (warmupTime > 0) {
			plugin.messageBuilder.compose(player, messageId)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
					.setMacro(Macro.WORLD, plugin.getServer().getWorld(destination.getWorldUid()))
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
	private void loadDestinationChunk(final Destination destination) {

		Location location = destination.getLocation();

		if (location != null && location.getWorld() != null) {
			if (!location.getWorld().getChunkAt(location).isLoaded()) {
				location.getWorld().getChunkAt(location).load();
			}
		}
	}


	/**
	 * Check if player is within configured minimum distance from destination
	 *
	 * @param player      the player
	 * @param destination the destination
	 * @return true if under minimum distance, false if not
	 */
	private boolean isUnderMinimumDistance(final Player player, final Destination destination) {
		return destination.getLocation() != null
				&& destination.getLocation().getWorld() != null
				&& player.getWorld().equals(destination.getLocation().getWorld())
				&& player.getLocation().distanceSquared(destination.getLocation()) < Math.pow(plugin.getConfig().getInt("minimum-distance"), 2);
	}


	/**
	 * Log teleport item use
	 *
	 * @param player the player being logged as using a lodestar item
	 */
	private void logUsage(final Player player, final Destination destination) {
		if (plugin.getConfig().getBoolean("log-use")) {

			CommandSender console = plugin.getServer().getConsoleSender();

			// get destination name
			String destinationName = destination.getDisplayName();

			// write message to log
			console.sendMessage(player.getName() + ChatColor.RESET + " used a "
					+ plugin.messageBuilder.getItemName() + ChatColor.RESET  + " to "
					+ ChatColor.AQUA + destinationName + ChatColor.RESET + " in "
					+ plugin.worldManager.getWorldName(player) + ChatColor.RESET + ".");
		}
	}

}
