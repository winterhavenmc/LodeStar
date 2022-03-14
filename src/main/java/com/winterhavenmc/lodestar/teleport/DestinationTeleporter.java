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

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


class DestinationTeleporter extends AbstractTeleporter implements Teleporter {

	private final TeleportExecutor teleportExecutor;


	DestinationTeleporter(final PluginMain plugin, final TeleportExecutor teleportExecutor) {
		super(plugin);
		this.teleportExecutor = teleportExecutor;
	}


	/**
	 * Begin teleport to destination determined by LodeStar item key
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get item key
		String key = plugin.lodeStarFactory.getKey(playerItem);

		// get destination for key
		Optional<Destination> optionalDestination = plugin.dataStore.selectRecord(key);

		// if destination is valid, execute teleport
		if (optionalDestination.isPresent()) {

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			removeFromInventoryOnUse(player, playerItem);

			// execute teleport
			execute(player, optionalDestination.get(), playerItem, MessageId.TELEPORT_WARMUP);
		}
		else {
			// send invalid destination message
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION).setMacro(Macro.DESTINATION, plugin.messageBuilder.getSpawnDisplayName()).send();
		}
	}


	@Override
	public void execute(final Player player, final Destination finalDestination, final ItemStack playerItem, final MessageId messageId) {
		teleportExecutor.execute(player, finalDestination, playerItem, messageId);
	}

}
