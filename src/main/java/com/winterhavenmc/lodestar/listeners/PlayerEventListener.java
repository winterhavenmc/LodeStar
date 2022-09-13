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

package com.winterhavenmc.lodestar.listeners;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;


/**
 * Implements event listener for LodeStar
 *
 * @author Tim Savage
 * @version 1.0
 */
public final class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// set to hold craft table materials
	private final Set<Material> craftTables =  Set.of(
			Material.CARTOGRAPHY_TABLE,
			Material.CRAFTING_TABLE,
			Material.FLETCHING_TABLE,
			Material.SMITHING_TABLE,
			Material.LOOM,
			Material.STONECUTTER );



	/**
	 * constructor method for PlayerEventListener class
	 *
	 * @param    plugin        A reference to this plugin's main class
	 */
	public PlayerEventListener(final PluginMain plugin) {

		// reference to main
		Objects.requireNonNull(this.plugin = plugin);

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Check if cancel on interaction is configured and if such interaction has occurred
	 *
	 * @param event the event to check for player/block interaction
	 * @return true if cancellable interaction occurred, false if not
	 */
	boolean cancelTeleportOnInteraction(final PlayerInteractEvent event) {

		final Player player = event.getPlayer();
		final Action action = event.getAction();
		final EquipmentSlot hand = event.getHand();

		// if cancel-on-interaction is configured true, and player is in warmup hashmap,
		// and player is interacting with a block (not air) then cancel teleport, output message and return
		if (plugin.getConfig().getBoolean("cancel-on-interaction")
				&& plugin.teleportHandler.isWarmingUp(player)
				&& (Action.LEFT_CLICK_BLOCK.equals(action) || Action.RIGHT_CLICK_BLOCK.equals(action))) {

			// if item used is in off_hand, do nothing and return
			if (EquipmentSlot.OFF_HAND.equals(hand)) {
				return true;
			}

			// cancel teleport and send message, play sound
			plugin.teleportHandler.cancelTeleport(player);
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_CANCELLED_INTERACTION).send();
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
			return true;
		}
		return false;
	}


	/**
	 * Check if block is of type that interaction is allowed while holding a lode star item in hand
	 *
	 * @param block the block being interacted with
	 * @return true if block type is allowed for interactionm, false if not
	 */
	boolean allowedInteraction(Block block) {
		// allow use of doors, gates and trap doors with item in hand
		if (block.getBlockData() instanceof Openable) {
			return true;
		}

		// allow use of switches with item in hand
		if (block.getBlockData() instanceof Switch) {
			return true;
		}

		// allow use of containers and other tile entity blocks with item in hand
		if (block.getState() instanceof TileState) {
			return true;
		}

		// allow use of crafting tables with item in hand
		//noinspection RedundantIfStatement
		if (craftTables.contains(block.getType())) {
			return true;
		}
		return false;
	}


	/**
	 *
	 * @param event the event whose action to test
	 * @return true if action is allowed, false if action would cancel the event
	 */
	boolean allowedClickType(PlayerInteractEvent event) {

		// if event action is PHYSICAL (not left-click or right click), do nothing and return
		if (event.getAction().equals(Action.PHYSICAL)) {
			return true;
		}

		// if event action is left-click, and left-click is config disabled, do nothing and return
		return event.getAction().equals(Action.LEFT_CLICK_BLOCK)
				|| event.getAction().equals(Action.LEFT_CLICK_AIR)
				&& !plugin.getConfig().getBoolean("left-click");
	}


	boolean isNotAir(final Block block) {
		return block != null && !block.getType().isAir();
	}


	boolean isNotSneaking(final Player player) {
		return !player.isSneaking();
	}


	/**
	 * Event listener for PlayerInteractEvent<br>
	 * detects LodeStar use, or cancels teleport
	 * if cancel-on-interaction configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerUse(final PlayerInteractEvent event) {

		// get event player
		final Player player = event.getPlayer();

		// perform check for cancel-on-interaction
		if (cancelTeleportOnInteraction(event)) {
			return;
		}

		// if item used is not a LodeStar, do nothing and return
		if (!plugin.lodeStarUtility.isItem(event.getItem())) {
			return;
		}

		// perform check for allowed click type
		if (allowedClickType(event)) {
			return;
		}

		// if player is not warming
		if (!plugin.teleportHandler.isWarmingUp(player)) {

			// check if clicked block is air (null)
			// check that player is not sneaking
			// check allowed to interact with blocks
			if (isNotAir(event.getClickedBlock())
					&& isNotSneaking(event.getPlayer())
					&& allowedInteraction(event.getClickedBlock())) {
				return;
			}

			// cancel event
			event.setCancelled(true);

			// if players current world is not enabled in config, send message and return
			if (!plugin.worldManager.isEnabled(player.getWorld())) {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_WORLD_DISABLED).send();
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_DENIED_WORLD_DISABLED);
				return;
			}

			// if player does not have lodestar.use permission, send message and return
			if (!player.hasPermission("lodestar.use")) {
				plugin.messageBuilder.compose(player, MessageId.PERMISSION_DENIED_USE).send();
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_DENIED_PERMISSION);
				return;
			}

			// if shift-click configured and player is not sneaking, send teleport fail shift-click message and return
			if (plugin.getConfig().getBoolean("shift-click") && !player.isSneaking()) {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_SHIFT_CLICK).send();
				return;
			}

			// initiate teleport
			plugin.teleportHandler.initiateTeleport(player);
		}
	}


	/**
	 * cancel any pending teleports on player death
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerDeath(final PlayerDeathEvent event) {
		// cancel any pending teleport for player
		plugin.teleportHandler.cancelTeleport(event.getEntity());
	}


	/**
	 * clean up any pending player tasks when player logs off of server
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerQuit(final PlayerQuitEvent event) {
		// cancel any pending teleport for player
		plugin.teleportHandler.cancelTeleport(event.getPlayer());
	}


	/**
	 * Prevent LodeStar items from being used in crafting recipes if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onCraftPrepare(final PrepareItemCraftEvent event) {

		// if allow-in-recipes is true in configuration, do nothing and return
		if (plugin.getConfig().getBoolean("allow-in-recipes")) {
			return;
		}

		// if crafting inventory contains LodeStar item, set result item to null
		for (ItemStack itemStack : event.getInventory()) {
			if (plugin.lodeStarUtility.isItem(itemStack)) {
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
	void onEntityDamage(final EntityDamageEvent event) {

		// if cancel-on-damage configuration is true, check if damaged entity is player
		if (plugin.getConfig().getBoolean("cancel-on-damage")) {

			Entity entity = event.getEntity();

			// if damaged entity is player, check for pending teleport
			if (entity instanceof Player player) {

				// if player is in warmup hashmap, cancel teleport and send player message
				if (plugin.teleportHandler.isWarmingUp(player)) {
					plugin.teleportHandler.cancelTeleport(player);
					plugin.messageBuilder.compose(player, MessageId.TELEPORT_CANCELLED_DAMAGE).send();
					plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
				}
			}
		}
	}


	/**
	 * cancels player teleport if cancel-on-movement configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerMovement(final PlayerMoveEvent event) {

		// if cancel-on-movement configuration is false, do nothing and return
		if (!plugin.getConfig().getBoolean("cancel-on-movement")) {
			return;
		}

		Player player = event.getPlayer();

		// if player is in warmup hashmap, cancel teleport and send player message
		if (plugin.teleportHandler.isWarmingUp(player)) {

			// check for player movement other than head turning
			if (event.getFrom().distanceSquared(Objects.requireNonNull(event.getTo())) > 0) {
				plugin.teleportHandler.cancelTeleport(player);
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_CANCELLED_MOVEMENT).send();
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
			}
		}
	}

}
