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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.winterhavenmc.util.TimeUnit.SECONDS;


abstract class Teleporter {

	protected final PluginMain plugin;
	protected final WarmupMap warmupMap;

	Teleporter(final PluginMain plugin, final WarmupMap warmupMap) {
		this.plugin = plugin;
		this.warmupMap = warmupMap;
	}


	abstract void initiate(final Player player);


	/**
	 * Execute the teleport to destination
	 *
	 * @param player the player to teleport
	 * @param finalDestination the destination
	 * @param playerItem the LodeStar item used to initiate teleport
	 * @param messageId the teleport warmup message to send to player
	 */
	public void execute(final Player player, final Destination finalDestination, final ItemStack playerItem, final MessageId messageId) {

		// if finalDestination location is null, send invalid destination message and return
		if (finalDestination.getLocation() == null) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, finalDestination.getDisplayName())
					.send();
			return;
		}

		// if player is less than configured minimum distance from destination, send player proximity message and return
		if (isUnderMinimumDistance(player, finalDestination)) {
			plugin.messageBuilder.build(player, MessageId.TELEPORT_FAIL_PROXIMITY)
					.setMacro(Macro.DESTINATION, finalDestination.getDisplayName())
					.send();
			return;
		}

		// initiate delayed teleport for player to final destination
		BukkitTask teleportTask = new DelayedTeleportTask(plugin, player, finalDestination, playerItem.clone())
				.runTaskLater(plugin, SECONDS.toTicks(plugin.getConfig().getLong("teleport-warmup")));

		// if configured warmup time is greater than zero, send warmup message
		long warmupTime = plugin.getConfig().getLong("teleport-warmup");
		if (warmupTime > 0) {
			plugin.messageBuilder.compose(player, messageId)
					.setMacro(Macro.DESTINATION, finalDestination.getDisplayName())
					.setMacro(Macro.WORLD, plugin.getServer().getWorld(finalDestination.getWorldUid()))
					.setMacro(Macro.DURATION, SECONDS.toMillis(warmupTime))
					.send();

			// if enabled, play teleport warmup sound effect
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_WARMUP);
		}

		// insert player and taskId into warmup hashmap
		warmupMap.startPlayerWarmUp(player, teleportTask.getTaskId());

		// load destination chunk if not already loaded
		loadDestinationChunk(finalDestination);

		// if log-use is enabled in config, write log entry
		logUsage(player);
	}


	/**
	 * Get bedspawn location for a player
	 *
	 * @param player the player
	 * @return the player bedspawn location wrapped in an {@link Optional}
	 */
	private Optional<Location> getHomeLocation(final Player player) {

		// if player is null, return empty optional
		if (player == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(player.getBedSpawnLocation());
	}


	/**
	 * Get bedspawn destination for a player
	 *
	 * @param player the player
	 * @return the player bedspawn destination wrapped in an {@link Optional}
	 */
	protected Optional<Destination> getHomeDestination(final Player player) {

		// if player is null, return empty optional
		if (player == null) {
			return Optional.empty();
		}

		Optional<Location> optionalLocation = getHomeLocation(player);
		if (optionalLocation.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new Destination(plugin.messageBuilder.getHomeDisplayName().orElse("Home"), optionalLocation.get()));
	}


	/**
	 * Get spawn location for a player
	 *
	 * @param player the player
	 * @return the player spawn location wrapped in an {@link Optional}
	 */
	private Optional<Location> getSpawnLocation(final Player player) {

		// if player is null, return empty optional
		if (player == null) {
			return Optional.empty();
		}
		return Optional.of(player.getWorld().getSpawnLocation());
	}


	/**
	 * Get spawn destination for a player
	 *
	 * @param player the player
	 * @return the player spawn destination wrapped in an {@link Optional}
	 */
	protected Optional<Destination> getSpawnDestination(final Player player) {

		// if player is null, return empty optional
		if (player == null) {
			return Optional.empty();
		}

		Optional<Location> optionalLocation = getSpawnLocation(player);
		if (optionalLocation.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new Destination(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"), optionalLocation.get()));
	}


	/**
	 * Get overworld spawn location corresponding to a player nether or end world.
	 *
	 * @param player the passed player whose current world will be used to find a matching over world spawn location
	 * @return {@link Optional} wrapped spawn location of the normal world associated with the passed player
	 * nether or end world, or the current player world spawn location if no matching normal world found
	 */
	protected Optional<Location> getOverworldSpawnLocation(final Player player) {

		// check for null parameter
		if (player == null) {
			return Optional.empty();
		}

		// create list to store normal environment worlds
		List<World> normalWorlds = new ArrayList<>();

		// iterate through all server worlds
		for (World checkWorld : plugin.getServer().getWorlds()) {

			// if world is normal environment, try to match name to passed world
			if (checkWorld.getEnvironment().equals(World.Environment.NORMAL)) {

				// check if normal world matches passed world minus nether/end suffix
				if (checkWorld.getName().equals(player.getWorld().getName().replaceFirst("(_nether$|_the_end$)", ""))) {
					return Optional.of(plugin.worldManager.getSpawnLocation(checkWorld));
				}

				// if no match, add to list of normal worlds
				normalWorlds.add(checkWorld);
			}
		}

		// if only one normal world exists, return that world
		if (normalWorlds.size() == 1) {
			return Optional.of(plugin.worldManager.getSpawnLocation(normalWorlds.get(0)));
		}

		// if no matching normal world found and more than one normal world exists, return passed world spawn location
		return Optional.of(plugin.worldManager.getSpawnLocation(player.getWorld()));
	}


	/**
	 * Check if a player is in a nether world
	 *
	 * @param player the player
	 * @return true if player is in a nether world, false if not
	 */
	protected boolean isInNetherWorld(final Player player) {
		return player.getWorld().getEnvironment().equals(World.Environment.NETHER);
	}


	/**
	 * Check if a player is in an end world
	 *
	 * @param player the player
	 * @return true if player is in an end world, false if not
	 */
	protected boolean isInEndWorld(final Player player) {
		return player.getWorld().getEnvironment().equals(World.Environment.THE_END);
	}


	/**
	 * Preload chunk at teleport destination if not already loaded
	 *
	 * @param destination the destination location
	 */
	protected void loadDestinationChunk(final Destination destination) {

		Location location = destination.getLocation();

		if (location != null && location.getWorld() != null) {
			if (!location.getWorld().getChunkAt(location).isLoaded()) {
				location.getWorld().getChunkAt(location).load();
			}
		}
	}


	/**
	 * Log teleport item use
	 *
	 * @param player the player being logged as using a lodestar item
	 */
	protected void logUsage(final Player player) {
		if (plugin.getConfig().getBoolean("log-use")) {

			// write message to log
			plugin.getLogger().info(player.getName() + ChatColor.RESET + " used a "
					+ plugin.messageBuilder.getItemName() + ChatColor.RESET + " in "
					+ plugin.worldManager.getWorldName(player) + ChatColor.RESET + ".");
		}
	}


	/**
	 * Check if player is within configured minimum distance from destination
	 *
	 * @param player the player
	 * @param destination the destination
	 * @return true if under minimum distance, false if not
	 */
	private boolean isUnderMinimumDistance(final Player player, final Destination destination) {
		 return destination.getLocation() != null
				 && destination.getLocation().getWorld() != null
				 && player.getWorld().equals(destination.getLocation().getWorld())
				 && player.getLocation().distanceSquared(destination.getLocation()) < Math.pow(plugin.getConfig().getInt("minimum-distance"), 2);
	}

}
