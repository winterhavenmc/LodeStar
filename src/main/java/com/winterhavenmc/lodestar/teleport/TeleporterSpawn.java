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


class TeleporterSpawn extends Teleporter {


	TeleporterSpawn(final PluginMain plugin, final WarmupMap warmupMap) {
		super(plugin, warmupMap);
	}


	/**
	 * Begin teleport to world spawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	void initiate(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get spawn destination
		Optional<Destination> optionalDestination = getSpawnDestination(player);

		if (optionalDestination.isPresent()) {

			// get location from destination
			Location location = optionalDestination.get().getLocation();

			// if from-nether is enabled in config and player is in nether, try to get overworld spawn location
			if (plugin.getConfig().getBoolean("from-nether") && isInNetherWorld(player)) {
				location = getOverworldSpawnLocation(player).orElse(location);
			}
			// if from-end is enabled in config and player is in end, try to get overworld spawn location
			else if (plugin.getConfig().getBoolean("from-end") && isInEndWorld(player)) {
				location = getOverworldSpawnLocation(player).orElse(location);
			}

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			String removeItem = plugin.getConfig().getString("remove-from-inventory");
			if (removeItem != null && removeItem.equalsIgnoreCase("on-use")) {
				playerItem.setAmount(playerItem.getAmount() - 1);
				player.getInventory().setItemInMainHand(playerItem);
			}

			// create final destination object
			Destination finalDestination = new Destination(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"), location);

			// initiate delayed teleport for player to final destination
			execute(player, finalDestination, playerItem, MessageId.TELEPORT_WARMUP_SPAWN);
		}
		else {
			// send invalid destination message
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, plugin.messageBuilder.getSpawnDisplayName())
					.send();
		}
	}

}
