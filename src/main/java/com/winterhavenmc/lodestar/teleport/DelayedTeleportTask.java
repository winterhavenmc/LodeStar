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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;


/**
 * Class that extends BukkitRunnable to teleport a player to a predefined location
 * after a configured warmup period.
 */
final class DelayedTeleportTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;
	private final Destination destination;
	private final ItemStack playerItem;
	private Location location;
	private BukkitTask particleTask;


	/**
	 * Class constructor method
	 *
	 * @param player      the player to be teleported
	 * @param destination the teleport destination
	 * @param playerItem  the item used to initiate teleport
	 */
	DelayedTeleportTask(final PluginMain plugin, final Player player, final Destination destination, final ItemStack playerItem) {

		this.plugin = plugin;
		this.player = player;
		this.destination = destination;
		this.playerItem = playerItem;
		this.location = destination.getLocation().orElse(null);

		// start repeating task for generating particles at player location
		if (plugin.getConfig().getBoolean("particle-effects")) {

			// start particle task with 2 tick delay, so it doesn't self cancel on first run
			particleTask = new ParticleTask(plugin, player).runTaskTimer(plugin, 2L, 10);

		}
	}


	@Override
	public void run() {

		// cancel particles task
		particleTask.cancel();

		// if player is in warmup map
		if (plugin.teleportHandler.isWarmingUp(player)) {

			// remove player from warmup map
			plugin.teleportHandler.removeWarmingUpPlayer(player);

			// if destination is spawn, get spawn location from world manager
			if (destination.isSpawn()) {
				location = plugin.worldManager.getSpawnLocation(Objects.requireNonNull(location.getWorld()));
			}

			// if remove-from-inventory is configured on-success, take one LodeStar item from inventory now
			if ("on-success".equalsIgnoreCase(plugin.getConfig().getString("remove-from-inventory"))) {

				// try to remove one LodeStar item from player inventory
				boolean wasRemoved = false;
				for (ItemStack itemStack : player.getInventory()) {
					if (playerItem.isSimilar(itemStack)) {
						ItemStack itemToRemove = itemStack.clone();
						itemToRemove.setAmount(1);
						player.getInventory().removeItem(itemToRemove);
						wasRemoved = true;
						break;
					}
				}

				// if one LodeStar item could not be removed from inventory, send message, set cooldown and return
				if (!wasRemoved) {
					plugin.messageBuilder.compose(player, MessageId.TELEPORT_CANCELLED_NO_ITEM).send();
					plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED_NO_ITEM);
					plugin.teleportHandler.startPlayerCooldown(player);
					return;
				}
			}
			// play pre-teleport sound if sound effects are enabled
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_DEPARTURE);

			// teleport player to location
			player.teleport(location);

			// if destination is spawn, send spawn specific success message
			if (destination.isSpawn()) {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_SUCCESS_SPAWN)
						.setMacro(Macro.DESTINATION, plugin.messageBuilder.getSpawnDisplayName())
						.setMacro(Macro.DESTINATION_WORLD, plugin.getServer().getWorld(destination.getWorldUid()))
						.send();
			}
			// otherwise, send regular success message
			else {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_SUCCESS)
						.setMacro(Macro.DESTINATION, destination.getDisplayName())
						.send();
			}
			// play post-teleport sound if sound effects are enabled
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_SUCCESS_ARRIVAL);

			// if lightning is enabled in config, strike lightning at teleport destination
			if (plugin.getConfig().getBoolean("lightning")) {
				player.getWorld().strikeLightningEffect(location);
			}

			// start player cooldown
			plugin.teleportHandler.startPlayerCooldown(player);
		}
	}

}
