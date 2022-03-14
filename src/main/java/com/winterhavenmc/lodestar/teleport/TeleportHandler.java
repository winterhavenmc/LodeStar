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
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;

import com.winterhavenmc.lodestar.storage.Destination;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


/**
 * Class that manages player teleportation, including warmup and cooldown.
 */
public final class TeleportHandler {

	// reference to main class
	private final PluginMain plugin;

	// map containing player UUID as key and warmup task id as value
	private final WarmupMap warmupMap;

	// map to store player UUID and cooldown expire time in milliseconds
	private final CooldownMap cooldownMap;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public TeleportHandler(final PluginMain plugin) {
		this.plugin = plugin;
		this.warmupMap = new WarmupMap(plugin);
		this.cooldownMap = new CooldownMap(plugin);
	}


	/**
	 * Start the player teleport
	 *
	 * @param player the player being teleported
	 */
	public void initiateTeleport(final Player player) {

		// if player is warming up, do nothing and return
		if (warmupMap.isWarmingUp(player)) {
			return;
		}

		// if player cooldown has not expired, send player cooldown message and return
		if (cooldownMap.isCoolingDown(player)) {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_COOLDOWN)
					.setMacro(Macro.DURATION, cooldownMap.getCooldownTimeRemaining(player))
					.send();
			return;
		}

		// get key from player item
		String key = plugin.lodeStarFactory.getKey(player.getInventory().getItemInMainHand());

		Teleporter teleporter;

		// if item key is home key, teleport to bed spawn location
		if (Destination.isHome(key)) {
			teleporter = new HomeTeleporter(plugin, new TeleportExecutor(plugin, warmupMap));
		}
		// if item key is spawn key, teleport to world spawn location
		else if (Destination.isSpawn(key)) {
			teleporter = new SpawnTeleporter(plugin, new TeleportExecutor(plugin, warmupMap));
		}
		// teleport to destination for key
		else {
			teleporter = new DestinationTeleporter(plugin, new TeleportExecutor(plugin, warmupMap));
		}

		// initiate teleport
		teleporter.initiate(player);
	}


	/**
	 * Insert player into cooldown map
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	public void startPlayerCooldown(final Player player) {
		cooldownMap.startPlayerCooldown(player);
	}


	/**
	 * Cancel pending teleport for player
	 *
	 * @param player the player to cancel teleport
	 */
	public void cancelTeleport(final Player player) {

		// if player is in warmup hashmap, cancel delayed teleport task and remove player from warmup hashmap
		if (warmupMap.containsPlayer(player)) {

			// get delayed teleport task id
			int taskId = warmupMap.getTaskId(player);

			// cancel delayed teleport task
			plugin.getServer().getScheduler().cancelTask(taskId);

			// remove player from warmup hashmap
			warmupMap.removePlayer(player);
		}
	}


	/**
	 * Check if player is in teleport initiated set. Public pass through method.
	 *
	 * @param player the player to check if teleport is initiated
	 * @return {@code true} if teleport been initiated, {@code false} if it has not
	 */
	public boolean isInitiated(final Player player) {
		return warmupMap.isInitiated(player);
	}


	/**
	 * Test if player uuid is in warmup hashmap. Public pass through method.
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	public boolean isWarmingUp(final Player player) {
		return warmupMap.isWarmingUp(player);
	}


	/**
	 * Remove player uuid from warmup hashmap. Public pass through method.
	 *
	 * @param player the player to remove from the warmup map
	 */
	public void removeWarmingUpPlayer(final Player player) {
		warmupMap.removePlayer(player);
	}


	/**
	 * Get bedspawn destination for a player
	 *
	 * @param player the player
	 * @return the player bedspawn destination wrapped in an {@link Optional}
	 */
	Optional<Destination> getHomeDestination(final Player player) {

		// if player is null, return empty optional
		if (player == null) {
			return Optional.empty();
		}

		// get player bed spawn location
		Location location = player.getBedSpawnLocation();

		// if location is null, return empty optional
		if (location == null) {
			return Optional.empty();
		}

		// return optional wrapped destination for player bed spawn location
		return Optional.of(new Destination(plugin.messageBuilder.getHomeDisplayName().orElse("Home"), location));
	}


	/**
	 * Get spawn destination for a player
	 *
	 * @param player the player
	 * @return the player spawn destination wrapped in an {@link Optional}
	 */
	Optional<Destination> getSpawnDestination(final Player player) {

		// if player is null, return empty optional
		if (player == null) {
			return Optional.empty();
		}

		// get spawn location for player
		Location location = plugin.worldManager.getSpawnLocation(player);

		// if location is null, return empty optional
		if (location == null) {
			return Optional.empty();
		}

		// return destination for player spawn
		return Optional.of(new Destination(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"), location));
	}


	/**
	 * remove one lode star item from player inventory
	 * @param player     the player
	 * @param playerItem the item
	 */
	void removeFromInventory(Player player, ItemStack playerItem) {
		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		String removeItem = plugin.getConfig().getString("remove-from-inventory");
		if (removeItem != null && removeItem.equalsIgnoreCase("on-use")) {
			playerItem.setAmount(playerItem.getAmount() - 1);
			player.getInventory().setItemInMainHand(playerItem);
		}
	}

}
