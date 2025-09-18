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

package com.winterhavenmc.lodestar.plugin.listeners;

import com.winterhavenmc.library.messagebuilder.ItemForge;
import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.teleport.TeleportHandler;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.util.SoundId;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;


/**
 * Implements event listener for LodeStar
 *
 * @author Tim Savage
 * @version 1.0
 */
public final class PlayerEventListener implements Listener
{
	private final TeleportHandler teleportHandler;
	private final LodeStarPluginController.ListenerContextContainer ctx;


	/**
	 * constructor method for PlayerEventListener class
	 */
	public PlayerEventListener(final TeleportHandler teleportHandler, final LodeStarPluginController.ListenerContextContainer ctx)
	{
		this.teleportHandler = teleportHandler;
		this.ctx = ctx;

		// register events in this class
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	/**
	 * cancel any pending teleports on player death
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerDeath(final PlayerDeathEvent event)
	{
		// cancel any pending teleport for player
		teleportHandler.cancelTeleport(event.getEntity());
	}


	/**
	 * clean up any pending player tasks when player logs off of server
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerQuit(final PlayerQuitEvent event)
	{
		// cancel any pending teleport for player
		teleportHandler.cancelTeleport(event.getPlayer());
	}


	/**
	 * Prevent LodeStar items from being used in crafting recipes if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onCraftPrepare(final PrepareItemCraftEvent event)
	{
		// if allow-in-recipes is true in configuration, do nothing and return
		if (ctx.plugin().getConfig().getBoolean("allow-in-recipes"))
		{
			return;
		}

		// if crafting inventory contains LodeStar item, set result item to null
		for (ItemStack itemStack : event.getInventory())
		{
			if (ItemForge.isCustomItem(itemStack))
			{
				event.getInventory().setResult(null);
			}
		}
	}


	/**
	 * Cancels pending teleport if cancel-on-damage configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityDamage(final EntityDamageEvent event)
	{
		// if cancel-on-damage configuration is true, check if damaged entity is player
		if (ctx.plugin().getConfig().getBoolean("cancel-on-damage"))
		{
			Entity entity = event.getEntity();

			// if damaged entity is player, and player has pending teleport, cancel teleport and send player message
			if (entity instanceof Player player && teleportHandler.isWarmingUp(player))
			{
				teleportHandler.cancelTeleport(player);
				ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_CANCELLED_DAMAGE).send();
				ctx.soundConfig().playSound(player, SoundId.TELEPORT_CANCELLED);
			}
		}
	}


	/**
	 * cancels player teleport if cancel-on-movement configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerMovement(final PlayerMoveEvent event)
	{
		// if cancel-on-movement configuration is false, do nothing and return
		if (!ctx.plugin().getConfig().getBoolean("cancel-on-movement"))
		{
			return;
		}

		Player player = event.getPlayer();

		// if player is pending teleport and has moved, cancel teleport and send player message
		if (teleportHandler.isWarmingUp(player) && playerHasMoved(event))
		{
			teleportHandler.cancelTeleport(player);
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_CANCELLED_MOVEMENT).send();
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_CANCELLED);
		}
	}


	/**
	 * check for player movement other than head turning
	 *
	 * @param event the player move event
	 * @return true if player has moved, false if not
	 */
	private boolean playerHasMoved(PlayerMoveEvent event)
	{
		return event.getFrom().distanceSquared(Objects.requireNonNull(event.getTo())) > 0;
	}

}
