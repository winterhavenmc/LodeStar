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
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;


public class PlayerInteractEventListener implements Listener
{
	private final TeleportHandler teleportHandler;
	private final LodeStarPluginController.ContextContainer ctx;

	// set to hold craft table materials
	private final Set<Material> craftTables = Set.of(
			Material.CARTOGRAPHY_TABLE,
			Material.CRAFTING_TABLE,
			Material.FLETCHING_TABLE,
			Material.SMITHING_TABLE,
			Material.LOOM,
			Material.STONECUTTER);


	/**
	 * constructor method for PlayerInteractEventListener class
	 */
	public PlayerInteractEventListener(final TeleportHandler teleportHandler, final LodeStarPluginController.ContextContainer ctx)
	{
		this.teleportHandler = teleportHandler;
		this.ctx = ctx;

		// register events in this class
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	/**
	 * Event listener for PlayerInteractEvent<br>
	 * detects LodeStar use, or cancels teleport
	 * if cancel-on-interaction configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerInteract(final PlayerInteractEvent event)
	{
		// get event player
		final Player player = event.getPlayer();

		// perform check for cancel-on-interaction
		if (cancelTeleportOnInteraction(event)
				|| !ItemForge.isCustomItem(event.getItem())
				|| allowedClickType(event))
		{
			return;
		}

		// if player is not warming
		// check if clicked block is not air, and player is not sneaking, and  block interaction type is allowed
		//noinspection ConstantConditions
		if (!teleportHandler.isWarmingUp(player)
				&& isNotAir(event.getClickedBlock())
				&& isNotSneaking(event.getPlayer())
				&& allowedInteraction(event.getClickedBlock()))
		{
			return;
		}

		// cancel event
		event.setCancelled(true);

		// if players current world is not enabled in config, send message and return
		if (!ctx.worldManager().isEnabled(player.getWorld()))
		{
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_FAIL_WORLD_DISABLED).send();
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_DENIED_WORLD_DISABLED);
		}
		// if player does not have lodestar.use permission, send message and return
		else if (!player.hasPermission("lodestar.use"))
		{
			ctx.messageBuilder().compose(player, MessageId.EVENT_ITEM_USE_PERMISSION_DENIED).send();
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_DENIED_PERMISSION);
		}
		// if shift-click configured and player is not sneaking, send teleport fail shift-click message and return
		else if (ctx.plugin().getConfig().getBoolean("shift-click") && isNotSneaking(player))
		{
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_FAIL_SHIFT_CLICK).send();
		}
		else
		{
			teleportHandler.initiateTeleport(player);
		}
	}


	/**
	 * Check if cancel on interaction is configured and if such interaction has occurred
	 *
	 * @param event the event to check for player/block interaction
	 * @return true if cancellable interaction occurred, false if not
	 */
	boolean cancelTeleportOnInteraction(final PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		final Action action = event.getAction();
		final EquipmentSlot hand = event.getHand();

		// if cancel-on-interaction is configured true, and player is in warmup hashmap,
		// and player is interacting with a block (not air) then cancel teleport, output message and return
		if (ctx.plugin().getConfig().getBoolean("cancel-on-interaction")
				&& teleportHandler.isWarmingUp(player)
				&& (Action.LEFT_CLICK_BLOCK.equals(action) || Action.RIGHT_CLICK_BLOCK.equals(action)))
		{
			// if item used is in off_hand, do nothing and return
			if (EquipmentSlot.OFF_HAND.equals(hand))
			{
				return true;
			}

			// cancel teleport and send message, play sound
			teleportHandler.cancelTeleport(player);
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_CANCELLED_INTERACTION).send();
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_CANCELLED);
			return true;
		}
		return false;
	}


	/**
	 * Check if block is of type that interaction is allowed while holding a lode star item in hand
	 *
	 * @param block the block being interacted with
	 * @return true if block type is allowed for interaction, false if not
	 */
	boolean allowedInteraction(Block block)
	{
		// allow use of doors, gates and trap doors with item in hand
		if (block.getBlockData() instanceof Openable)
		{
			return true;
		}

		// allow use of switches with item in hand
		if (block.getBlockData() instanceof Switch)
		{
			return true;
		}

		// allow use of containers and other tile entity blocks with item in hand
		if (block.getState() instanceof TileState)
		{
			return true;
		}

		// allow use of crafting tables with item in hand
		return craftTables.contains(block.getType());
	}


	/**
	 * Check if a player click is an allowed action
	 *
	 * @param event the event whose action to test
	 * @return true if action is allowed, false if action would cancel the event
	 */
	boolean allowedClickType(PlayerInteractEvent event)
	{
		// if event action is PHYSICAL (not left-click or right click), do nothing and return
		if (event.getAction().equals(Action.PHYSICAL))
		{
			return true;
		}

		// if event action is left-click, and left-click is config disabled, do nothing and return
		return event.getAction().equals(Action.LEFT_CLICK_BLOCK)
				|| event.getAction().equals(Action.LEFT_CLICK_AIR)
				&& !ctx.plugin().getConfig().getBoolean("left-click");
	}


	/**
	 * Check if a block is not an air block
	 *
	 * @param block the block to check
	 * @return true if block is not air, false if it is
	 */
	boolean isNotAir(final Block block)
	{
		return block != null && !block.getType().isAir();
	}


	/**
	 * Check if a player is not sneaking
	 *
	 * @param player the player to check
	 * @return true if player is not sneaking, false if it is
	 */
	boolean isNotSneaking(final Player player)
	{
		return !player.isSneaking();
	}

}
