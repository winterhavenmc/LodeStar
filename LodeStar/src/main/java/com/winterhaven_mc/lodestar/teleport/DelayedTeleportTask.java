package com.winterhaven_mc.lodestar.teleport;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.MessageId.*;
import static com.winterhaven_mc.lodestar.messages.Macro.*;


/**
 * Class that extends BukkitRunnable to teleport a player to a predefined location
 * after a configured warmup period.
 */
class DelayedTeleportTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;
	private Location location;
	private BukkitTask particleTask;
	private final Destination destination;
	private final ItemStack playerItem;

	/**
	 * Class constructor method
	 *
	 * @param player the player to be teleported
	 * @param destination the teleport destination
	 * @param playerItem the item used to initiate teleport
	 */
	DelayedTeleportTask(final PluginMain plugin,
						final Player player,
						final Destination destination,
						final ItemStack playerItem) {

		// check for null parameters
		Objects.requireNonNull(this.plugin = plugin);
		Objects.requireNonNull(this.player = player);
		Objects.requireNonNull(this.destination = destination);
		Objects.requireNonNull(this.playerItem = playerItem);
		Objects.requireNonNull(this.location = destination.getLocation());

		// start repeating task for generating particles at player location
		if (plugin.getConfig().getBoolean("particle-effects")) {

			// start particle task, with 2 tick delay so it doesn't self cancel on first run
			particleTask = new ParticleTask(plugin, player).runTaskTimer(plugin, 2L, 10);

		}
	}


	@Override
	public void run() {

		// cancel particles task
		particleTask.cancel();

		// if player is in warmup map
		if (plugin.teleportManager.isWarmingUp(player)) {

			// remove player from warmup map
			plugin.teleportManager.removePlayer(player);

			// if destination is spawn, get spawn location from world manager
			if (destination.isSpawn()) {
				location = plugin.worldManager.getSpawnLocation(Objects.requireNonNull(location.getWorld()));
			}

			// if remove-from-inventory is configured on-success, take one LodeStar item from inventory now
			if (Objects.requireNonNull(plugin.getConfig().getString("remove-from-inventory"))
					.equalsIgnoreCase("on-success")) {

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
					plugin.messageBuilder.build(player, TELEPORT_CANCELLED_NO_ITEM).send(plugin.languageHandler);
					plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED_NO_ITEM);
					plugin.teleportManager.setPlayerCooldown(player);
					return;
				}
			}
			// play pre-teleport sound if sound effects are enabled
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_DEPARTURE);

			// teleport player to location
			player.teleport(location);

			// if destination is spawn, send spawn specific success message
			if (destination.isSpawn()) {
				plugin.messageBuilder.build(player, TELEPORT_SUCCESS_SPAWN)
						.setMacro(DESTINATION, plugin.languageHandler.getSpawnDisplayName())
						.setMacro(WORLD, plugin.getServer().getWorld(destination.getWorldUid()))
						.send(plugin.languageHandler);
			}
			// otherwise send regular success message
			else {
				plugin.messageBuilder.build(player, TELEPORT_SUCCESS)
						.setMacro(DESTINATION, destination.getDisplayName())
						.send(plugin.languageHandler);
			}
			// play post-teleport sound if sound effects are enabled
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_ARRIVAL);

			// if lightning is enabled in config, strike lightning at teleport destination
			if (plugin.getConfig().getBoolean("lightning")) {
				player.getWorld().strikeLightningEffect(location);
			}

			// set player cooldown
			plugin.teleportManager.setPlayerCooldown(player);
		}
	}

}
