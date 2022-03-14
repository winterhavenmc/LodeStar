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
import com.winterhavenmc.lodestar.storage.Destination;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


/**
 * This class provides methods that are common to the concrete Teleporter classes.
 * It is not intended to be subclassed, except to provide these common methods to
 * the Teleporter classes within this package. The methods are declared final to
 * prevent them being overridden, and the class and methods are declared package-private
 * to prevent their use outside this package.
 */
abstract class AbstractTeleporter {

	protected PluginMain plugin;

	public AbstractTeleporter(final PluginMain plugin) {
		this.plugin = plugin;
	}


	/**
	 * Get bedspawn destination for a player
	 *
	 * @param player the player
	 * @return the player bedspawn destination wrapped in an {@link Optional}
	 */
	final Optional<Destination> getHomeDestination(final Player player) {

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
	final Optional<Destination> getSpawnDestination(final Player player) {

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
	final void removeFromInventoryOnUse(Player player, ItemStack playerItem) {
		// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
		String removeItem = plugin.getConfig().getString("remove-from-inventory");
		if (removeItem != null && removeItem.equalsIgnoreCase("on-use")) {
			playerItem.setAmount(playerItem.getAmount() - 1);
			player.getInventory().setItemInMainHand(playerItem);
		}
	}

}
