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

import java.util.*;
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

		// if player cooldown has not expired, send player cooldown message and return
		if (isCoolingDown(player)) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_COOLDOWN)
					.setMacro(Macro.DURATION, getCooldownTimeRemaining(player))
					.send();
			return;
		}

		// if player is warming up, do nothing and return
		if (isWarmingUp(player)) {
			return;
		}

		// get key from player item
		String key = plugin.lodeStarFactory.getKey(player.getInventory().getItemInMainHand());

		// if destination key is home, teleport to bed spawn location
		if (isHomeKey(key)) {
			teleportToHome(player);
		}
		// if destination key is spawn, teleport to world spawn location
		else if (isSpawnKey(key)) {
			teleportToSpawn(player);
		}
		// teleport to destination for key
		else {
			teleportToDestination(player);
		}
	}


	/**
	 * Begin teleport to players bedspawn destination
	 *
	 * @param player the player to teleport
	 */
	private void teleportToHome(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get home destination
		Optional<Destination> optionalDestination = getHomeDestination(player);

		if (optionalDestination.isPresent()) {

			Location location = optionalDestination.get().getLocation();

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			String removeItem = plugin.getConfig().getString("remove-from-inventory");
			if (removeItem != null && removeItem.equalsIgnoreCase("on-use")) {
				playerItem.setAmount(playerItem.getAmount() - 1);
				player.getInventory().setItemInMainHand(playerItem);
			}

			// create final destination object
			Destination finalDestination = new Destination(plugin.messageBuilder.getHomeDisplayName().orElse("Home"), location);

			// initiate delayed teleport for player to final destination
			executeTeleport(player, finalDestination, playerItem, MessageId.TELEPORT_WARMUP);
		}
		else if (plugin.getConfig().getBoolean("bedspawn-fallback")) {
			teleportToSpawn(player);
		}
		else {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_NO_BEDSPAWN).send();
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
		}
	}


	/**
	 * Begin teleport to world spawn destination
	 *
	 * @param player the player to teleport
	 */
	private void teleportToSpawn(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get spawn destination
		Optional<Destination> optionalDestination = getSpawnDestination(player);

		if (optionalDestination.isPresent()) {

			// get location from destination
			Location location = optionalDestination.get().getLocation();

			// if from-nether is enabled in config and player is in nether, try to get overworld spawn location
			if (plugin.getConfig().getBoolean("from-nether") && isInNetherWorld(player)) {
				location = getOverworldSpawnLocation(player).orElse(location);
			}
			// if from-end is enabled in config and player is in end, try to get overworld spawn location
			else if (plugin.getConfig().getBoolean("from-end") && isInEndWorld(player)) {
				location = getOverworldSpawnLocation(player).orElse(location);
			}

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			String removeItem = plugin.getConfig().getString("remove-from-inventory");
			if (removeItem != null && removeItem.equalsIgnoreCase("on-use")) {
				playerItem.setAmount(playerItem.getAmount() - 1);
				player.getInventory().setItemInMainHand(playerItem);
			}

			// create final destination object
			Destination finalDestination = new Destination(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"), location);

			// initiate delayed teleport for player to final destination
			executeTeleport(player, finalDestination, playerItem, MessageId.TELEPORT_WARMUP_SPAWN);
		}
		else {
			// send invalid destination message
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, plugin.messageBuilder.getSpawnDisplayName())
					.send();
		}
	}


	/**
	 * Begin teleport to destination determined by LodeStar item key
	 *
	 * @param player the player to teleport
	 */
	private void teleportToDestination(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get item key
		String key = plugin.lodeStarFactory.getKey(playerItem);

		// get spawn destination for key
		Optional<Destination> optionalDestination = plugin.dataStore.selectRecord(key);

		if (optionalDestination.isPresent()) {

			// get location for destination
			Location location = optionalDestination.get().getLocation();

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			String removeItem = plugin.getConfig().getString("remove-from-inventory");
			if (removeItem != null && removeItem.equalsIgnoreCase("on-use")) {
				playerItem.setAmount(playerItem.getAmount() - 1);
				player.getInventory().setItemInMainHand(playerItem);
			}

			// create final destination object
			Destination finalDestination = new Destination(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"), location);

			executeTeleport(player, finalDestination, playerItem, MessageId.TELEPORT_WARMUP);
		}
		else {
			// send invalid destination message
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, plugin.messageBuilder.getSpawnDisplayName())
					.send();
		}
	}


	/**
	 * Execute the teleport to destination
	 *
	 * @param player the player to teleport
	 * @param finalDestination the destination
	 * @param playerItem the LodeStar item used to initiate teleport
	 * @param messageId the teleport warmup message to send to player
	 */
	private void executeTeleport(final Player player, final Destination finalDestination, final ItemStack playerItem, final MessageId messageId) {

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
		putPlayer(player, teleportTask.getTaskId());

		// load destination chunk if not already loaded
		loadDestinationChunk(finalDestination);

		// if log-use is enabled in config, write log entry
		logUsage(player);
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
		}.runTaskLater(plugin, SECONDS.toTicks(cooldownSeconds));
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
	private boolean isHomeKey(final String key) {

		// if key is null, return false
		if (key == null) {
			return false;
		}

		// if key is literal string "home", return true
		if (key.equalsIgnoreCase("home")) {
			return true;
		}

		// if key matches configured home display name, return true; otherwise return false
		return key.equals(plugin.lodeStarFactory.deriveKey(plugin.messageBuilder.getHomeDisplayName().orElse("home")));
	}


	/**
	 * Test if destination key represents spawn location
	 *
	 * @param key the destination key to examine
	 * @return true if key is for spawn destination, false if not
	 */
	private boolean isSpawnKey(final String key) {

		// if key is null, return false
		if (key == null) {
			return false;
		}

		// if key is literal string "spawn" return true
		if (key.equalsIgnoreCase("spawn")) {
			return true;
		}

		// if key matches configured spawn display name, return true; otherwise return false
		return key.equals(plugin.lodeStarFactory.deriveKey(plugin.messageBuilder.getSpawnDisplayName().orElse("spawn")));
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
	 * Get bedspawn destination for a player
	 *
	 * @param player the player
	 * @return the player bedspawn destination wrapped in an {@link Optional}
	 */
	private Optional<Destination> getHomeDestination(final Player player) {

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
	 * Get spawn destination for a player
	 *
	 * @param player the player
	 * @return the player spawn destination wrapped in an {@link Optional}
	 */
	private Optional<Destination> getSpawnDestination(final Player player) {

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
	private Optional<Location> getOverworldSpawnLocation(final Player player) {

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
					return Optional.of(checkWorld.getSpawnLocation());
				}

				// if no match, add to list of normal worlds
				normalWorlds.add(checkWorld);
			}
		}

		// if only one normal world exists, return that world
		if (normalWorlds.size() == 1) {
			return Optional.of(normalWorlds.get(0).getSpawnLocation());
		}

		// if no matching normal world found and more than one normal world exists, return passed world spawn location
		return Optional.of(player.getWorld().getSpawnLocation());
	}


	/**
	 * Check if a player is in a nether world
	 *
	 * @param player the player
	 * @return true if player is in a nether world, false if not
	 */
	private boolean isInNetherWorld(final Player player) {
		return player.getWorld().getEnvironment().equals(World.Environment.NETHER);
	}


	/**
	 * Check if a player is in an end world
	 *
	 * @param player the player
	 * @return true if player is in an end world, false if not
	 */
	private boolean isInEndWorld(final Player player) {
		return player.getWorld().getEnvironment().equals(World.Environment.THE_END);
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
