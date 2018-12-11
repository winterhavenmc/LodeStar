package com.winterhaven_mc.lodestar.teleport;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.MessageId;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;
import org.bukkit.Location;
//import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

class DelayedTeleportTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;
	private Location location;
	private BukkitTask particleTask;
	private Destination destination;
	private ItemStack playerItem;


	/**
	 * Class constructor method
	 */
	DelayedTeleportTask(final Player player, final Destination destination, final ItemStack playerItem) {
		
		this.plugin = PluginMain.instance;
		this.player = player;
		this.destination = destination;
		this.playerItem = playerItem;
		
		this.location = destination.getLocation();
		
		// start repeating task for generating particles at player location
		if (plugin.getConfig().getBoolean("particle-effects")) {

			// start particle task, with 2 tick delay so it doesn't self cancel on first run
			particleTask = new ParticleTask(player).runTaskTimer(plugin, 2L, 10);
		
		}
	}

	@Override
	public void run() {

		// cancel particles task
		particleTask.cancel();
		
		// if player is in warmup hashmap
		if (plugin.teleportManager.isWarmingUp(player)) {

			// remove player from warmup hashmap
			plugin.teleportManager.removePlayer(player);
		
			// if destination is spawn, get spawn location from world manager
			if (destination.isSpawn()) {
				location = plugin.worldManager.getSpawnLocation(location.getWorld());
			}

			// if remove-from-inventory is configured on-success, take one LodeStar item from inventory now
			if (plugin.getConfig().getString("remove-from-inventory").equalsIgnoreCase("on-success")) {
				
				// try to remove one LodeStar item from player inventory
				//HashMap<Integer,ItemStack> notRemoved = new HashMap<Integer,ItemStack>();
				boolean notRemoved = true;
				for (ItemStack itemStack : player.getInventory()) {
					if (playerItem.isSimilar(itemStack)) {
						ItemStack removeItem = itemStack.clone();
						removeItem.setAmount(1);
						player.getInventory().removeItem(removeItem);
						notRemoved = false;
						break;
					}
				}
				
				// if one LodeStar item could not be removed from inventory, send message, set cooldown and return
				if (notRemoved) {
					plugin.messageManager.sendMessage(player,MessageId.TELEPORT_CANCELLED_NO_ITEM);
					plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED_NO_ITEM);
					plugin.teleportManager.setPlayerCooldown(player);
					return;
				}
			}
			// play pre-teleport sound if sound effects are enabled
			plugin.soundConfig.playSound(player,SoundId.TELEPORT_SUCCESS_DEPARTURE);

			// teleport player to location
			player.teleport(location);

			// if destination is spawn, send spawn specific success message
			if (destination.isSpawn()) {
				plugin.messageManager.sendMessage(player,MessageId.TELEPORT_SUCCESS_SPAWN,plugin.messageManager.getSpawnDisplayName());
			}
			// otherwise send regular success message
			else {
				plugin.messageManager.sendMessage(player,MessageId.TELEPORT_SUCCESS,destination.getDisplayName());
			}
			// play post-teleport sound if sound effects are enabled
			plugin.soundConfig.playSound(player,SoundId.TELEPORT_SUCCESS_ARRIVAL);

			// if lightning is enabled in config, strike lightning at teleport destination
			if (plugin.getConfig().getBoolean("lightning")) {
				player.getWorld().strikeLightningEffect(location);
			}
			
			// set player cooldown
			plugin.teleportManager.setPlayerCooldown(player);
		}
	}
	
}
