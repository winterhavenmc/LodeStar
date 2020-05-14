package com.winterhaven_mc.lodestar.teleport;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.SimpleAPI;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.messages.MessageId;
import com.winterhaven_mc.lodestar.storage.Destination;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class TeleportManager {

	// reference to main class
	private final PluginMain plugin;

	// HashMap containing player UUID as key and warmup task id as value
	private final ConcurrentHashMap<UUID, Integer> warmupMap;

	// hashmap to store player UUID and cooldown expire time in milliseconds
	private final ConcurrentHashMap<UUID, Long> cooldownMap;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public TeleportManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize warmup HashMap
		warmupMap = new ConcurrentHashMap<>();

		// initialize cooldown map
		cooldownMap = new ConcurrentHashMap<>();
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
			plugin.messageManager.sendMessage(player, MessageId.TELEPORT_COOLDOWN);
			return;
		}

		// if player is warming up, do nothing and return
		if (isWarmingUp(player)) {
			return;
		}

		// get destination from player item
		String key = SimpleAPI.getKey(playerItem);
		Location location = null;
		Destination destination = null;

		// if destination key equals home, get player bed spawn location
		if (key != null && (key.equalsIgnoreCase("home")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName())))) {

			location = player.getBedSpawnLocation();

			// if bedspawn location is not null, create destination with bed spawn location
			if (location != null) {
				destination = new Destination("home", plugin.messageManager.getHomeDisplayName(), location);
				if (plugin.debug) {
					plugin.getLogger().info("destination is home. Location: " + location.toString());
				}
			}
			// otherwise if bedspawn-fallback is true in config, set key to spawn
			else if (plugin.getConfig().getBoolean("bedspawn-fallback")) {
				key = "spawn";
			}
			// if bedspawn location is null and bedspawn-fallback is false, send message and return
			else {
				plugin.messageManager.sendMessage(player, MessageId.TELEPORT_FAIL_NO_BEDSPAWN);
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
				return;
			}
		}

		// if destination is spawn, get spawn location
		if (key != null && (key.equalsIgnoreCase("spawn")
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName())))) {

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
			String displayName = plugin.messageManager.getSpawnDisplayName();
			destination = new Destination(key, displayName, location);
		}

		// if destination is not set to home or spawn get destination from storage
		if (destination == null) {
			// get destination from storage
			destination = plugin.dataStore.getRecord(key);
			if (destination != null) {
				location = destination.getLocation();
			}
			else {
				location = null;
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

			plugin.messageManager.sendMessage(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION, 1, displayName);
			return;
		}

		// if player is less than config min-distance from destination, send player proximity message and return
		if (player.getWorld() == location.getWorld()
				&& location.distance(player.getLocation()) < plugin.getConfig().getInt("minimum-distance")) {
			plugin.messageManager.sendMessage(player, MessageId.TELEPORT_FAIL_PROXIMITY, 1, destination.getDisplayName());
			return;
		}

		// send debug message to log
		if (plugin.debug) {
			plugin.getLogger().info("Teleporting to destination: " + location.toString());
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
		if (plugin.getConfig().getInt("teleport-warmup") > 0) {

			// if destination is spawn send spawn specific warmup message
			if (destination.isSpawn()) {
				plugin.messageManager.sendMessage(player, MessageId.TELEPORT_WARMUP_SPAWN, destination.getDisplayName());
			}
			// otherwise send regular warmup message
			else {
				plugin.messageManager.sendMessage(player, MessageId.TELEPORT_WARMUP, destination.getDisplayName());
			}
			// if enabled, play sound effect
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_WARMUP);
		}

		// initiate delayed teleport for player to destination
		BukkitTask teleportTask = new DelayedTeleportTask(player, destination,
				playerItem.clone()).runTaskLater(plugin, plugin.getConfig().getInt("teleport-warmup") * 20);

		// insert player and taskId into warmup hashmap
		putPlayer(player, teleportTask.getTaskId());

		// if log-use is enabled in config, write log entry
		if (plugin.getConfig().getBoolean("log-use")) {

			// write message to log
			plugin.getLogger().info(player.getName() + ChatColor.RESET + " used a "
					+ plugin.messageManager.getItemName() + ChatColor.RESET + " in "
					+ plugin.messageManager.getWorldName(player) + ChatColor.RESET + ".");
		}
	}


	/**
	 * Insert player uuid and taskId into warmup hashmap.
	 *
	 * @param player the player to be inserted in the warmup map
	 * @param taskId the taskId of the player's delayed teleport task
	 */
	private void putPlayer(final Player player, final Integer taskId) {
		warmupMap.put(player.getUniqueId(), taskId);
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
	 * Test if player uuid is in warmup hashmap.
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	public boolean isWarmingUp(final Player player) {

		// if player is in warmup hashmap, return true, otherwise return false
		return warmupMap.containsKey(player.getUniqueId());
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
		}.runTaskLater(plugin, (cooldownSeconds * 20));
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


}
