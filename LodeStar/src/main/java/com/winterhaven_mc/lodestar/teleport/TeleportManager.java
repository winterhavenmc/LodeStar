package com.winterhaven_mc.lodestar.teleport;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.SimpleAPI;
import com.winterhaven_mc.lodestar.messages.MessageId;
import com.winterhaven_mc.lodestar.messages.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class TeleportManager {

    // reference to main class
    private final PluginMain plugin;

    // HashMap of player UUIDs and warmup times
    private ConcurrentHashMap<UUID,Integer> warmupMap;

    // hashmap to store player uuids and cooldown expire times
    private ConcurrentHashMap<UUID, Long> cooldownMap;


    public TeleportManager (PluginMain plugin) {

        // set reference to main class
        this.plugin = plugin;

        // initialize warmup HashMap
        warmupMap = new ConcurrentHashMap<>();

        // initialize cooldown map
        cooldownMap = new ConcurrentHashMap<>();
    }


    /**
     * Start the player teleport
     * @param player the player being teleported
     */
    public final void initiateTeleport(final Player player) {

        final ItemStack playerItem = player.getInventory().getItemInMainHand();

        // if player cooldown has not expired, send player cooldown message and return
        if (getCooldownTimeRemaining(player) > 0) {
            plugin.messageManager.sendPlayerMessage(player, MessageId.TELEPORT_COOLDOWN);
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
                plugin.messageManager.sendPlayerMessage(player,MessageId.TELEPORT_FAIL_NO_BEDSPAWN);
                plugin.messageManager.sendPlayerSound(player,SoundId.TELEPORT_CANCELLED);
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
            location = plugin.worldManager.getSpawnLocation(location.getWorld());

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

            String displayName;

            // get display name
            if (destination != null) {
                displayName = destination.getDisplayName();
            }
            else {
                displayName = key;
            }

            plugin.messageManager.sendPlayerMessage(player,MessageId.TELEPORT_FAIL_INVALID_DESTINATION,1, displayName);
            return;
        }

        // if player is less than config min-distance from destination, send player proximity message and return
        if (player.getWorld() == location.getWorld()
                && location.distance(player.getLocation()) < plugin.getConfig().getInt("minimum-distance")) {
            plugin.messageManager.sendPlayerMessage(player,MessageId.TELEPORT_FAIL_PROXIMITY,1, destination.getDisplayName());
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
            playerItem.setAmount(playerItem.getAmount() - 1);
            player.getInventory().setItemInMainHand(playerItem);
        }

        // if warmup setting is greater than zero, send warmup message
        if (plugin.getConfig().getInt("teleport-warmup") > 0) {

            // if destination is spawn send spawn specific warmup message
            if (destination.isSpawn()) {
                plugin.messageManager.sendPlayerMessage(player,MessageId.TELEPORT_WARMUP_SPAWN,destination.getDisplayName());
            }
            // otherwise send regular warmup message
            else {
                plugin.messageManager.sendPlayerMessage(player,MessageId.TELEPORT_WARMUP,destination.getDisplayName());
            }
            // if enabled, play sound effect
            plugin.messageManager.sendPlayerSound(player, SoundId.TELEPORT_WARMUP);
        }

        // initiate delayed teleport for player to destination
        BukkitTask teleportTask = new DelayedTeleportTask(player, destination,
                playerItem.clone()).runTaskLater(plugin, plugin.getConfig().getInt("teleport-warmup") * 20);

        // insert player and taskId into warmup hashmap
        putPlayer(player, teleportTask.getTaskId());

        // if log-use is enabled in config, write log entry
        if (plugin.getConfig().getBoolean("log-use")) {

            // construct log message
            String configItemName = plugin.messageManager.getItemName();
            String logMessage = player.getName() + " just used a "
                    + configItemName + " in " + plugin.worldManager.getWorldName(player.getWorld()) + ".";

            // strip color codes from log message
            logMessage = logMessage.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");

            // write message to log
            plugin.getLogger().info(logMessage);
        }
    }


   /**
    * Insert player uuid and taskId into warmup hashmap.
    * @param player the player to be inserted in the warmup map
    * @param taskId the taskId of the player's delayed teleport task
    */
   private void putPlayer(final Player player, final Integer taskId) {
        warmupMap.put(player.getUniqueId(), taskId);
    }


    /**
     * Remove player uuid from warmup hashmap.
     * @param player the player to remove from the warmup map
     */
    public void removePlayer(final Player player) {
        warmupMap.remove(player.getUniqueId());
    }


    /**
     * Test if player uuid is in warmup hashmap.
     * @param player the player to test if in warmup map
     * @return {@code true} if player is in warmup map, {@code false} if not
     */
    public boolean isWarmingUp(final Player player) {

        // if player is in warmup hashmap, return true, otherwise return false
        return warmupMap.containsKey(player.getUniqueId());
    }


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
     * @param player the player being inserted into the cooldown map
     */
    void setPlayerCooldown(final Player player) {

        int cooldown_seconds = plugin.getConfig().getInt("teleport-cooldown");

        Long expiretime = System.currentTimeMillis() + (cooldown_seconds * 1000);
        cooldownMap.put(player.getUniqueId(), expiretime);
        new BukkitRunnable(){

            public void run() {
                cooldownMap.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, (cooldown_seconds * 20));
    }


    /**
     * Get time remaining for player cooldown
     * @param player the player whose cooldown time remaining is being retrieved
     * @return long remainingtime
     */
    public long getCooldownTimeRemaining(final Player player) {
        long remainingTime = 0;
        if (cooldownMap.containsKey(player.getUniqueId())) {
            remainingTime = (cooldownMap.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
        }
        return remainingTime;
    }


}
