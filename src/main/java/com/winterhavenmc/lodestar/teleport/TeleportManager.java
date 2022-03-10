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
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.winterhavenmc.lodestar.util.BukkitTime.SECONDS;


/**
 * Class that manages player teleportation, including warmup and cooldown.
 */
public final class TeleportManager {

	// reference to main class
	private final PluginMain plugin;

	// HashMap containing player UUID as key and warmup task id as value
	private final ConcurrentHashMap<UUID, Integer> warmupMap;

	// hashmap to store player UUID and cooldown expire time in milliseconds
	private final ConcurrentHashMap<UUID, Long> cooldownMap;

	// Map containing player uuid for teleport initiated
	private final Set<UUID> teleportInitiated;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public TeleportManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize warmup HashMap
		warmupMap = new ConcurrentHashMap<>();

		// initialize cooldown map
		cooldownMap = new ConcurrentHashMap<>();

		// initialize teleport initiated set
		teleportInitiated = ConcurrentHashMap.newKeySet();
	}


	/**
	 * Start the player teleport
	 *
	 * @param player the player being teleported
	 */
	public void initiateTeleport(final Player player) {

		final ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if player cooldown has not expired, send player cooldown message and return
		if (getCooldownTimeRemaining(player) > 0) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_COOLDOWN)
					.setMacro(Macro.DURATION, getCooldownTimeRemaining(player))
					.send();
			return;
		}

		// if player is warming up, do nothing and return
		if (isWarmingUp(player)) {
			return;
		}

		// get destination from player item
		String key = plugin.lodeStarFactory.getKey(playerItem);
		Location location = null;
		Destination destination = null;

		// if destination key is home, get player bed spawn location
		if (destinationIsHome(key)) {

			location = player.getBedSpawnLocation();

			// if bedspawn location is not null, create destination with bedspawn location
			if (location != null) {
				destination = new Destination("home", plugin.messageBuilder.getHomeDisplayName(), location);
				if (plugin.getConfig().getBoolean("debug")) {
					plugin.getLogger().info("destination is home. Location: " + location);
				}
			}
			// otherwise, if bedspawn-fallback is true in config, set key to spawn
			else if (plugin.getConfig().getBoolean("bedspawn-fallback")) {
				key = "spawn";
			}
			// if bedspawn location is null and bedspawn-fallback is false, send message and return
			else {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_NO_BEDSPAWN).send();
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
				return;
			}
		}

		// if destination key is spawn, get spawn location
		if (destinationIsSpawn(key)) {

			World playerWorld = player.getWorld();
			String overworldName = playerWorld.getName().replaceFirst("(_nether|_the_end)$", "");
			World overworld = plugin.getServer().getWorld(overworldName);

			location = playerWorld.getSpawnLocation();

			// if from-nether is enabled in config and player is in nether, try to get overworld spawn location
			if (plugin.getConfig().getBoolean("from-nether") &&
					playerWorld.getName().endsWith("_nether") &&
					overworld != null) {
				location = overworld.getSpawnLocation();
			}

			// if from-end is enabled in config, and player is in end, try to get overworld spawn location
			if (plugin.getConfig().getBoolean("from-end") &&
					playerWorld.getName().endsWith("_the_end") &&
					overworld != null) {
				location = overworld.getSpawnLocation();
			}

			// if multiverse is enabled get spawn location from it, so we have pitch and yaw
			location = plugin.worldManager.getSpawnLocation(Objects.requireNonNull(location.getWorld()));

			// create warp object to send to delayed teleport method
			String displayName = plugin.messageBuilder.getSpawnDisplayName();
			destination = new Destination(key, displayName, location);
		}

		// if destination did not get set to home or spawn, get destination from storage
		if (destination == null) {
			// get destination from storage
			Optional<Destination> optionalDestination = plugin.dataStore.selectRecord(key);
			if (optionalDestination.isPresent()) {

				// unwrap optional destination
				destination = optionalDestination.get();

				// get location from destination
				location = destination.getLocation();
			}
		}

		// if location is null, send player message and return
		if (location == null) {

			String displayName;

			// get display name
			if (destination != null) {
				displayName = destination.getDisplayName();
			}
			else {
				displayName = key;
			}

			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, displayName)
					.send();
			return;
		}

		// if player is less than config min-distance from destination, send player proximity message and return
		if (player.getWorld() == location.getWorld()
				&& location.distance(player.getLocation()) < plugin.getConfig().getInt("minimum-distance")) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_PROXIMITY)
					.setMacro(Macro.DESTINATION, destination.getDisplayName())
					.send();
			return;
		}

		// send debug message to log
		if (plugin.getConfig().getBoolean("debug")) {
			plugin.getLogger().info("Teleporting to destination: " + location);
		}

		// load destination chunk if not already loaded
		String worldName = Objects.requireNonNull(location.getWorld()).getName();
		if (!Objects.requireNonNull(plugin.getServer().getWorld(worldName)).getChunkAt(location).isLoaded()) {
			Objects.requireNonNull(plugin.getServer().getWorld(worldName)).getChunkAt(location).load();
		}

		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		if (Objects.requireNonNull(plugin.getConfig().getString("remove-from-inventory")).equalsIgnoreCase("on-use")) {
			playerItem.setAmount(playerItem.getAmount() - 1);
			player.getInventory().setItemInMainHand(playerItem);
		}

		// if warmup setting is greater than zero, send warmup message
		long warmupTime = plugin.getConfig().getLong("teleport-warmup");
		if (warmupTime > 0) {

			// if destination is spawn send spawn specific warmup message
			if (destination.isSpawn()) {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_WARMUP_SPAWN)
						.setMacro(Macro.DESTINATION, destination.getDisplayName())
						.setMacro(Macro.WORLD, plugin.getServer().getWorld(destination.getWorldUid()))
						.setMacro(Macro.DURATION, SECONDS.toMillis(warmupTime))
						.send();
			}
			// otherwise, send regular warmup message
			else {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_WARMUP)
						.setMacro(Macro.DESTINATION, destination.getDisplayName())
						.setMacro(Macro.DURATION, SECONDS.toMillis(warmupTime))
						.send();
			}
			// if enabled, play sound effect
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_WARMUP);
		}

		// initiate delayed teleport for player to destination
		BukkitTask teleportTask = new DelayedTeleportTask(plugin, player, destination,
				playerItem.clone()).runTaskLater(plugin, SECONDS.toTicks(warmupTime));

		// insert player and taskId into warmup hashmap
		putPlayer(player, teleportTask.getTaskId());

		// if log-use is enabled in config, write log entry
		logUsage(player);
	}


	/**
	 * Insert player uuid and taskId into warmup hashmap.
	 *
	 * @param player the player to be inserted in the warmup map
	 * @param taskId the taskId of the player's delayed teleport task
	 */
	private void putPlayer(final Player player, final Integer taskId) {

		// check for null parameter
		Objects.requireNonNull(player);

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


	/**
	 * Remove player uuid from warmup hashmap.
	 *
	 * @param player the player to remove from the warmup map
	 */
	public void removePlayer(final Player player) {
		warmupMap.remove(player.getUniqueId());
	}


	/**
	 * Test if player uuid is in warmup hashmap
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	public boolean isWarmingUp(final Player player) {

		// if player is in warmup hashmap, return true, otherwise return false
		return warmupMap.containsKey(player.getUniqueId());
	}


	/**
	 * Test if player is currently cooling down after item use
	 *
	 * @param player the player to check for cooldown
	 * @return boolean - {@code true} if player is cooling down after item use, {@code false} if not
	 */
	public boolean isCoolingDown(final Player player) {
		return getCooldownTimeRemaining(player) > 0;
	}


	/**
	 * Cancel pending teleport for player
	 *
	 * @param player the player to cancel teleport
	 */
	public void cancelTeleport(final Player player) {

		// if player is in warmup hashmap, cancel delayed teleport task and remove player from warmup hashmap
		if (warmupMap.containsKey(player.getUniqueId())) {

			// get delayed teleport task id
			int taskId = warmupMap.get(player.getUniqueId());

			// cancel delayed teleport task
			plugin.getServer().getScheduler().cancelTask(taskId);

			// remove player from warmup hashmap
			warmupMap.remove(player.getUniqueId());
		}
	}


	/**
	 * Insert player uuid into cooldown hashmap with {@code expiretime} as value.<br>
	 * Schedule task to remove player uuid from cooldown hashmap when time expires.
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void setPlayerCooldown(final Player player) {

		int cooldownSeconds = plugin.getConfig().getInt("teleport-cooldown");

		Long expireTime = System.currentTimeMillis() + (SECONDS.toMillis(cooldownSeconds));
		cooldownMap.put(player.getUniqueId(), expireTime);
		new BukkitRunnable() {

			public void run() {
				cooldownMap.remove(player.getUniqueId());
			}
		}.runTaskLater(plugin, (SECONDS.toTicks(cooldownSeconds)));
	}


	/**
	 * Get time remaining for player cooldown
	 *
	 * @param player the player whose cooldown time remaining is being retrieved
	 * @return long remaining time in milliseconds
	 */
	public long getCooldownTimeRemaining(final Player player) {
		long remainingTime = 0;
		if (cooldownMap.containsKey(player.getUniqueId())) {
			remainingTime = (cooldownMap.get(player.getUniqueId()) - System.currentTimeMillis());
		}
		return remainingTime;
	}


	/**
	 * Check if player is in teleport initiated set
	 *
	 * @param player the player to check if teleport is initiated
	 * @return {@code true} if teleport been initiated, {@code false} if it has not
	 */
	public boolean isInitiated(final Player player) {

		// check for null parameter
		if (player == null) {
			return false;
		}

		return !teleportInitiated.contains(player.getUniqueId());
	}


	/**
	 * Test if destination key represents home location
	 *
	 * @param key the destination key to examine
	 * @return true if key is for home destination, false if not
	 */
	private boolean destinationIsHome(final String key) {
		// if key is null, return false
		if (key == null) {
			return false;
		}
		// if key is literal string "home", return true
		if (key.equalsIgnoreCase("home")) {
			return true;
		}
		// if key matches configured home display name, return true; otherwise return false
		return key.equals(plugin.lodeStarFactory.deriveKey(plugin.messageBuilder.getHomeDisplayName()));
	}


	/**
	 * Test if destination key represents spawn location
	 *
	 * @param key the destination key to examine
	 * @return true if key is for spawn destination, false if not
	 */
	private boolean destinationIsSpawn(final String key) {
		// if key is null, return false
		if (key == null) {
			return false;
		}
		// if key is literal string "spawn" return true
		if (key.equalsIgnoreCase("spawn")) {
			return true;
		}
		// if key matches configured spawn display name, return true; otherwise return false
		return key.equals(plugin.lodeStarFactory.deriveKey(plugin.messageBuilder.getSpawnDisplayName()));
	}


	/**
	 * Log teleport item use
	 *
	 * @param player the player being logged as using a lodestar item
	 */
	private void logUsage(final Player player) {
		if (plugin.getConfig().getBoolean("log-use")) {

			// write message to log
			plugin.getLogger().info(player.getName() + ChatColor.RESET + " used a "
					+ plugin.messageBuilder.getItemName() + ChatColor.RESET + " in "
					+ plugin.worldManager.getWorldName(player) + ChatColor.RESET + ".");
		}
	}

}
