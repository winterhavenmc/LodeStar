package com.winterhaven_mc.lodestar;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

class DelayedTeleportTask extends BukkitRunnable {

	LodeStarMain plugin;
	Player player;
	Location location;
	BukkitTask particleTask;
	Destination destination;
	ItemStack playerItem;

	/**
	 * Class constructor method
	 */
	DelayedTeleportTask(final Player player, final Destination destination, final ItemStack playerItem) {
		
		this.plugin = LodeStarMain.instance;
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
		if (plugin.warmupManager.isWarmingUp(player)) {

			// remove player from warmup hashmap
			plugin.warmupManager.removePlayer(player);
		
			// if destination is spawn, check if multiverse enabled
			if (destination.isSpawn()) {
				
				// if multiverse is not enabled, copy pitch and yaw from player
				if (!plugin.mvEnabled) {
					location.setPitch(player.getLocation().getPitch());
					location.setYaw(player.getLocation().getYaw());
				}
			}
			
			// if remove-from-inventory is configured on-success, take one spawn star item from inventory now
			if (plugin.getConfig().getString("remove-from-inventory").equalsIgnoreCase("on-success")) {
				
				// try to remove one spawn star item from player inventory
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
				
				// if one spawn star item could not be removed from inventory, send message, set cooldown and return
				if (notRemoved) {
					plugin.messageManager.sendPlayerMessage(player, "teleport-cancelled-no-item");
					if (plugin.getConfig().getBoolean("sound-effects")) {
						player.playSound(player.getLocation(), Sound.DONKEY_ANGRY, 1, 1);
					}
					plugin.cooldownManager.setPlayerCooldown(player);
					return;
				}
			}
			// play pre-teleport sound if sound effects are enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
			}

			// teleport player to location
			player.teleport(location);

			// if destination is spawn, send spawn specific success message
			if (destination.isSpawn()) {
				plugin.messageManager.sendPlayerMessage(player, "teleport-success-spawn", plugin.messageManager.getSpawnDisplayName());
			}
			// otherwise send regular success message
			else {
				plugin.messageManager.sendPlayerMessage(player, "teleport-success", destination.getDisplayName());
			}
			// play post-teleport sound if sound effects are enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
			}
			// if lightning is enabled in config, strike lightning at teleport destination
			if (plugin.getConfig().getBoolean("lightning")) {
				player.getWorld().strikeLightningEffect(location);
			}
			
			// set player cooldown
			plugin.cooldownManager.setPlayerCooldown(player);

			// try to prevent player spawning inside block and suffocating
			preventSuffocation(player, location);
		}
	}

	
	private void preventSuffocation(final Player player, final Location spawnLoc) {
		
		final int spawnAir = player.getRemainingAir();
		
		new BukkitRunnable(){

			public void run() {
				if (player.getRemainingAir() < spawnAir) {
					player.teleport(spawnLoc.add(0,1,0));
					player.setRemainingAir(spawnAir);
				}
			}
		}.runTaskLater(plugin, 20);		
	}

}
