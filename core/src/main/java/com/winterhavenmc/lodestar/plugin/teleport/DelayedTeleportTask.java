/*
 * Copyright (c) 2022-2025 Tim Savage.
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

package com.winterhavenmc.lodestar.plugin.teleport;

import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.models.destination.SpawnDestination;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.util.SoundId;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;

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
final class DelayedTeleportTask extends BukkitRunnable
{

	private final LodeStarPluginController.TeleporterContextContainer ctx;
	private final Player player;
	private final ValidDestination validDestination;
	private final ItemStack playerItem;
	private final TeleportHandler teleportHandler;
	private Location location;
	private BukkitTask particleTask;


	/**
	 * Class constructor method
	 *
	 * @param player      the player to be teleported
	 * @param validDestination the teleport validDestination
	 * @param playerItem  the item used to initiate teleport
	 */
	DelayedTeleportTask(final TeleportHandler teleportHandler,
	                    final LodeStarPluginController.TeleporterContextContainer ctx,
	                    final Player player,
	                    final ValidDestination validDestination,
						final Location location,
	                    final ItemStack playerItem)
	{
		this.teleportHandler = teleportHandler;
		this.ctx = ctx;
		this.player = player;
		this.validDestination = validDestination;
		this.playerItem = playerItem;
		this.location = location;

		// start repeating task for generating particles at player location
		if (ctx.plugin().getConfig().getBoolean("particle-effects"))
		{

			// start particle task with 2 tick delay, so it doesn't self cancel on first run
			particleTask = new ParticleTask(player, teleportHandler).runTaskTimer(ctx.plugin(), 2L, 10);

		}
	}


	@Override
	public void run()
	{
		// cancel particles task
		particleTask.cancel();

		// if player is in warmup map
		if (teleportHandler.isWarmingUp(player))
		{
			// remove player from warmup map
			teleportHandler.removeWarmingUpPlayer(player);

			// if validDestination is spawn, get spawn location from world manager
			if (validDestination instanceof SpawnDestination)
			{
				location = ctx.worldManager().getSpawnLocation(Objects.requireNonNull(location.getWorld()));
			}

			// if remove-from-inventory is configured on-success, take one LodeStar item from inventory now
			if ("on-success".equalsIgnoreCase(ctx.plugin().getConfig().getString("remove-from-inventory")))
			{
				// try to remove one LodeStar item from player inventory
				boolean wasRemoved = false;
				for (ItemStack itemStack : player.getInventory())
				{
					if (playerItem.isSimilar(itemStack))
					{
						ItemStack itemToRemove = itemStack.clone();
						itemToRemove.setAmount(1);
						player.getInventory().removeItem(itemToRemove);
						wasRemoved = true;
						break;
					}
				}

				// if one LodeStar item could not be removed from inventory, send message, set cooldown and return
				if (!wasRemoved)
				{
					ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_CANCELLED_NO_ITEM).send();
					ctx.soundConfig().playSound(player, SoundId.TELEPORT_CANCELLED_NO_ITEM);
					teleportHandler.startPlayerCooldown(player);
					return;
				}
			}
			// play pre-teleport sound if sound effects are enabled
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_SUCCESS_DEPARTURE);

			// teleport player to location
			player.teleport(location);

			// if validDestination is spawn, send spawn specific success message
			if (validDestination instanceof SpawnDestination)
			{
				ctx.messageBuilder().compose(player, MessageId.  EVENT_TELEPORT_SUCCESS_SPAWN)
						.setMacro(Macro.DESTINATION, validDestination)
						.send();
			}
			// otherwise, send standard success message
			else
			{
				ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_SUCCESS_DESTINATION)
						.setMacro(Macro.DESTINATION, validDestination)
						.send();
			}
			// play post-teleport sound if sound effects are enabled
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_SUCCESS_ARRIVAL);

			// if lightning is enabled in config, strike lightning at teleport validDestination
			if (ctx.plugin().getConfig().getBoolean("lightning"))
			{
				player.getWorld().strikeLightningEffect(location);
			}

			// start player cooldown
			teleportHandler.startPlayerCooldown(player);
		}
	}

}
