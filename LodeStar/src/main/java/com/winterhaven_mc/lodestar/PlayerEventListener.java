package com.winterhaven_mc.lodestar;

import org.bukkit.Location;
//import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;


/**
 * Implements player event listener for <code>LodeStar</code> events.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
class PlayerEventListener implements Listener {

	// reference to main class
	private final LodeStarMain plugin;
	
	
	/**
	 * constructor method for <code>PlayerEventListener</code> class
	 * @param	plugin		A reference to this plugin's main class
	 */
	PlayerEventListener(LodeStarMain plugin) {
		
		// reference to main
		this.plugin = plugin;
		
		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
	}


	/**
	 * Event listener for PlayerInteractEvent<br>
	 * detects LodeStar use, or cancels teleport
	 * if cancel-on-interaction configured
	 * @param event
	 */
	@EventHandler
	void onPlayerUse(PlayerInteractEvent event) {

		// get player
		final Player player = event.getPlayer();
		
		// if cancel-on-interaction is configured true, check if player is in warmup hashmap
		if (plugin.getConfig().getBoolean("cancel-on-interaction")) {
			
			// if player is in warmup hashmap, check if they are interacting with a block (not air)
			if (plugin.warmupManager.isWarmingUp(player)) {

				// if player is interacting with a block, cancel teleport, output message and return
				if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {			
					plugin.warmupManager.cancelTeleport(player);
					plugin.messageManager.sendPlayerMessage(player, "teleport-cancelled-interaction");

					// play sound effects if enabled
					plugin.messageManager.playerSound(player, "teleport-fail");
					return;
				}
			}
		}
		
		// get players item in hand
		ItemStack playerItem = player.getItemInHand();

		// if item used is not a LodeStar, do nothing and return
		if (!plugin.utilities.isLodeStar(playerItem)) {
			return;
		}
		
		// if event action is not a left or right click, do nothing and return
		if (event.getAction() == Action.PHYSICAL) {
			return;
		}
		
		// if players current world is not enabled in config, do nothing and return
		if (!playerWorldEnabled(player)) {
			return;
		}
		
		// if player does not have LodeStar.use permission, send message and return
		if (!player.hasPermission("lodestar.use")) {
			plugin.messageManager.sendPlayerMessage(player, "permission-denied-use");
			plugin.messageManager.playerSound(player, "teleport-denied-permission");
			return;
		}
		
		// if shift-click is configured true and player is not sneaking, send message and return
		if (plugin.getConfig().getBoolean("shift-click") && !event.getPlayer().isSneaking()) {
			plugin.messageManager.sendPlayerMessage(player, "usage-shift-click");
			return;
		}
		
		// cancel event
		event.setCancelled(true);
		
		// if player cooldown has not expired, send player cooldown message and return
		if (plugin.cooldownManager.getTimeRemaining(player) > 0) {
			plugin.messageManager.sendPlayerMessage(player, "teleport-cooldown");
			return;
		}
		
		// if player is warming up, do nothing and return
		if (plugin.warmupManager.isWarmingUp(player)) {
			return;
		}
		
		// get destination from player item
		String key = plugin.utilities.getKey(playerItem);
		Location location = null;
		Destination destination = null;
		
		// if destination key equals home, get player bed spawn location
		if (key.equalsIgnoreCase("home") 
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			
			location = player.getBedSpawnLocation();
			
			// if bedspawn location is not null, create destination with bed spawn location
			if (location != null) {
				destination = new Destination("home", plugin.messageManager.getHomeDisplayName(), location);
				if (plugin.debug) {
					plugin.getLogger().info("destination is home. Location: " + location.toString());
				}
			}
			// otherwise set key to spawn
			else {
				key = "spawn";
			}
		}
		
		// if destination is spawn, get spawn location
		if (key.equalsIgnoreCase("spawn") 
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			
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
			if (plugin.mvEnabled) {
				location = plugin.mvCore.getMVWorldManager().getMVWorld(location.getWorld()).getSpawnLocation();
			}
			// otherwise set pitch and yaw from player
			else {
				location.setPitch(player.getLocation().getPitch());
				location.setYaw(player.getLocation().getYaw());
			}
			
			// create warp object to send to delayed teleport method
			String displayName = plugin.messageManager.getSpawnDisplayName();
			destination = new Destination(key,displayName,location);
			
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
			
			String displayName = "";
			
			// get display name
			if (destination != null) {
				displayName = destination.getDisplayName();
			}
			else {
				displayName = key;
			}
			
			plugin.messageManager.sendPlayerMessage(player, "teleport-fail-invalid-destination", 1, displayName);
			return;
		}
		
		// if player is less than config min-distance from destination, send player proximity message and return
		if (player.getWorld() == location.getWorld()
				&& location.distance(player.getLocation()) < plugin.getConfig().getInt("minimum-distance")) {
			plugin.messageManager.sendPlayerMessage(player, "teleport-fail-proximity", 1, destination.getDisplayName());
			return;
		}
		
		// send debug message to log
		if (plugin.debug) {
			plugin.getLogger().info("Teleporting to destination: " + location.toString());
		}
		
		
		// load destination chunk if not already loaded
		String worldName = location.getWorld().getName();
		if (!plugin.getServer().getWorld(worldName).getChunkAt(location).isLoaded()) {
			plugin.getServer().getWorld(worldName).getChunkAt(location).load();
		}
		
		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		if (plugin.getConfig().getString("remove-from-inventory").equalsIgnoreCase("on-use")) {
			ItemStack removeItem = playerItem;
			removeItem.setAmount(playerItem.getAmount() - 1);
			player.setItemInHand(removeItem);
		}
		
		// if warmup setting is greater than zero, send warmup message
		if (plugin.getConfig().getInt("teleport-warmup") > 0) {

			// if destination is spawn send spawn specific warmup message
			if (destination.isSpawn()) {
				plugin.messageManager.sendPlayerMessage(player, "teleport-warmup-spawn", destination.getDisplayName());
			}
			// otherwise send regular warmup message
			else {
				plugin.messageManager.sendPlayerMessage(player, "teleport-warmup", destination.getDisplayName());
			}
			// if enabled, play sound effect
			plugin.messageManager.playerSound(player, "teleport-warmup");
		}
		
		// initiate delayed teleport for player to destination
		BukkitTask teleportTask = new DelayedTeleportTask(player, destination, playerItem.clone()).runTaskLater(plugin, plugin.getConfig().getInt("teleport-warmup") * 20);
		
		// insert player and taskId into warmup hashmap
		plugin.warmupManager.putPlayer(player, teleportTask.getTaskId());
		
		// if log-use is enabled in config, write log entry
		if (plugin.getConfig().getBoolean("log-use")) {
			
			// construct log message
			String configItemName = plugin.messageManager.getItemName();
			String log_message = player.getName() + " just used a " + configItemName + " in " + player.getWorld().getName() + ".";
			
			// strip color codes from log message
			log_message = log_message.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
			
			// write message to log
			plugin.getLogger().info(log_message);
		}
		
	}
	
	
	/**
	 * Event listener for PlayerDeathEvent<br>
	 * removes player from warmup hashmap
	 * @param event
	 */
	@EventHandler
	void onPlayerDeath(PlayerDeathEvent event) {
		
		Player player = (Player)event.getEntity();
		
		// cancel any pending teleport for player
		plugin.warmupManager.removePlayer(player);
		
	}

	
	/**
	 * Event listener for PlayerQuitEvent<br>
	 * removes player from warmup or cooldown hashmap
	 * @param event
	 */
	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		
		// cancel any pending teleport for player
		plugin.warmupManager.removePlayer(player);
		
		// remove player from message cooldown map
		plugin.messageManager.removePlayerCooldown(player);
		
	}
	
	
	/**
	 * Event listener for PrepareItemCraftEvent<br>
	 * Prevent LodeStar items from being used in crafting recipes if configured
	 * @param event
	 */
	@EventHandler
	void onCraftPrepare(PrepareItemCraftEvent event) {

		// if allow-in-recipes is true in configuration, do nothing and return
		if (plugin.getConfig().getBoolean("allow-in-recipes")) {
			return;
		}

		// if crafting inventory contains LodeStar item, set result item to null
		for(ItemStack itemStack : event.getInventory()) {
			if (plugin.utilities.isLodeStar(itemStack)) {
				event.getInventory().setResult(null);
			}
		}
		
	}
	
	
	/**
	 * Event listener for EntityDamageByEntity event<br>
	 * Cancels pending teleport if cancel-on-damage configured
	 * @param event
	 */
	@EventHandler
	void onEntityDamage(EntityDamageEvent event) {
		
		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if cancel-on-damage configuration is true, check if damaged entity is player
		if (plugin.getConfig().getBoolean("cancel-on-damage")) {
			
			Entity entity = event.getEntity();

			// if damaged entity is player, check for pending teleport
			if (entity instanceof Player) {
				
				Player player = (Player) entity;
				
				// if player is in warmup hashmap, cancel teleport and send player message
				if (plugin.warmupManager.isWarmingUp(player)) {
					plugin.warmupManager.cancelTeleport(player);
					plugin.messageManager.sendPlayerMessage(player, "teleport-cancelled-damage");
					plugin.messageManager.playerSound(player, "teleport-cancelled");
				}
			}
		}
	}
	
	
	/**
	 * Event listener for player movement event<br>
	 * cancels player teleport if cancel-on-movement configured
	 * @param event
	 */
	@EventHandler
	void onPlayerMovement(PlayerMoveEvent event) {
				
		// if cancel-on-movement configuration is false, do nothing and return
		if (!plugin.getConfig().getBoolean("cancel-on-movement")) {
			return;
		}
			
		Player player = event.getPlayer();

		// if player is in warmup hashmap, cancel teleport and send player message
		if (plugin.warmupManager.isWarmingUp(player)) {

			// check for player movement other than head turning
			if (event.getFrom().distance(event.getTo()) > 0) {
				plugin.warmupManager.cancelTeleport(player);
				plugin.messageManager.sendPlayerMessage(player,"teleport-cancelled-movement");
				plugin.messageManager.playerSound(player, "teleport-cancelled");
			}
		}
	}

	
	/**
	 * Test if player world is enabled in config
	 * @param player
	 * @return
	 */
	private boolean playerWorldEnabled(Player player) {
		
		// if player world is in list of enabled worlds, return true
		if (plugin.commandManager.getEnabledWorlds().contains(player.getWorld().getName())) {
			return true;
		}
		
		// otherwise return false
		return false;
	}

}
