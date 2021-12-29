package com.winterhaven_mc.lodestar.teleport;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.winterhaven_mc.lodestar.messages.MessageId.*;
import static com.winterhaven_mc.lodestar.messages.Macro.*;


/**
 * Class that manages player teleportation, including warmup and cooldown.
 */
public class TeleportManager {

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
	public final void initiateTeleport(final Player player) {

		final ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if player cooldown has not expired, send player cooldown message and return
		if (getCooldownTimeRemaining(player) > 0) {
			plugin.messageBuilder.build(player, TELEPORT_COOLDOWN)
					.setMacro(DURATION, getCooldownTimeRemaining(player))
					.send(plugin.languageHandler);
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

		// if destination key equals home, get player bed spawn location
		if (key != null && (key.equalsIgnoreCase("home")
				|| key.equals(Destination.deriveKey(plugin.languageHandler.getHomeDisplayName())))) {

			location = player.getBedSpawnLocation();

			// if bedspawn location is not null, create destination with bed spawn location
			if (location != null) {
				destination = new Destination("home", plugin.languageHandler.getHomeDisplayName(), location);
				if (plugin.getConfig().getBoolean("debug")) {
					plugin.getLogger().info("destination is home. Location: " + location);
				}
			}
			// otherwise if bedspawn-fallback is true in config, set key to spawn
			else if (plugin.getConfig().getBoolean("bedspawn-fallback")) {
				key = "spawn";
			}
			// if bedspawn location is null and bedspawn-fallback is false, send message and return
			else {
				plugin.messageBuilder.build(player, TELEPORT_FAIL_NO_BEDSPAWN).send(plugin.languageHandler);
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
				return;
			}
		}

		// if destination is spawn, get spawn location
		if (key != null && (key.equalsIgnoreCase("spawn")
				|| key.equals(Destination.deriveKey(plugin.languageHandler.getSpawnDisplayName())))) {

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

			// if multiverse is enabled, get spawn location from it so we have pitch and yaw
			location = plugin.worldManager.getSpawnLocation(Objects.requireNonNull(location.getWorld()));

			// create warp object to send to delayed teleport method
			String displayName = plugin.languageHandler.getSpawnDisplayName();
			destination = new Destination(key, displayName, location);
		}

		// if destination is not set to home or spawn get destination from storage
		if (destination == null) {
			// get destination from storage
			destination = plugin.dataStore.selectRecord(key);
			if (destination != null) {
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

			plugin.messageBuilder.build(player, TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(DESTINATION, displayName)
					.send(plugin.languageHandler);
					return;
		}

		// if player is less than config min-distance from destination, send player proximity message and return
		if (player.getWorld() == location.getWorld()
				&& location.distance(player.getLocation()) < plugin.getConfig().getInt("minimum-distance")) {
			plugin.messageBuilder.build(player, TELEPORT_FAIL_PROXIMITY)
					.setMacro(DESTINATION, destination.getDisplayName())
					.send(plugin.languageHandler);
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
				plugin.messageBuilder.build(player, TELEPORT_WARMUP_SPAWN)
						.setMacro(DESTINATION, destination.getDisplayName())
						.setMacro(WORLD, plugin.getServer().getWorld(destination.getWorldUid()))
						.setMacro(DURATION, TimeUnit.SECONDS.toMillis(warmupTime))
						.send(plugin.languageHandler);
			}
			// otherwise send regular warmup message
			else {
				plugin.messageBuilder.build(player, TELEPORT_WARMUP)
						.setMacro(DESTINATION, destination.getDisplayName())
						.setMacro(DURATION, TimeUnit.SECONDS.toMillis(warmupTime))
						.send(plugin.languageHandler);
			}
			// if enabled, play sound effect
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_WARMUP);
		}

		// initiate delayed teleport for player to destination
		BukkitTask teleportTask = new DelayedTeleportTask(plugin, player, destination,
				playerItem.clone()).runTaskLater(plugin, warmupTime * 20);

		// insert player and taskId into warmup hashmap
		putPlayer(player, teleportTask.getTaskId());

		// if log-use is enabled in config, write log entry
		if (plugin.getConfig().getBoolean("log-use")) {

			// write message to log
			plugin.getLogger().info(player.getName() + ChatColor.RESET + " used a "
					+ plugin.languageHandler.getItemName() + ChatColor.RESET + " in "
					+ plugin.worldManager.getWorldName(player) + ChatColor.RESET + ".");
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
	 * Insert player uuid into cooldown hashmap with <code>expiretime</code> as value.<br>
	 * Schedule task to remove player uuid from cooldown hashmap when time expires.
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void setPlayerCooldown(final Player player) {

		int cooldownSeconds = plugin.getConfig().getInt("teleport-cooldown");

		Long expireTime = System.currentTimeMillis() + (TimeUnit.SECONDS.toMillis(cooldownSeconds));
		cooldownMap.put(player.getUniqueId(), expireTime);
		new BukkitRunnable() {

			public void run() {
				cooldownMap.remove(player.getUniqueId());
			}
		}.runTaskLater(plugin, (cooldownSeconds * 20L));
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
	public final boolean isInitiated(final Player player) {

		// check for null parameter
		if (player == null) {
			return false;
		}

		return !teleportInitiated.contains(player.getUniqueId());
	}

}
