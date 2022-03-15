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
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


final class HomeTeleporter extends AbstractTeleporter implements Teleporter {

	private final WarmupMap warmupMap;


	HomeTeleporter(final PluginMain plugin, final WarmupMap warmupMap) {
		super(plugin);
		this.warmupMap = warmupMap;
	}


	/**
	 * Begin teleport to players bedspawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get home destination
		Optional<Destination> optionalDestination = getHomeDestination(player);

		if (optionalDestination.isPresent() && optionalDestination.get().getLocation() != null) {

			// get location from destination
			Location location = optionalDestination.get().getLocation();

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			removeFromInventoryOnUse(player, playerItem);

			// create final destination object
			Destination finalDestination = new Destination(plugin.messageBuilder.getHomeDisplayName().orElse("Home"), location);

			// initiate delayed teleport for player to final destination
			execute(player, finalDestination, playerItem, MessageId.TELEPORT_WARMUP);
		}
		else if (plugin.getConfig().getBoolean("bedspawn-fallback")) {

			Optional<Destination> spawnDestination = getSpawnDestination(player);
			if (spawnDestination.isPresent()) {
				Teleporter teleporterSpawn = new SpawnTeleporter(plugin, warmupMap);
				teleporterSpawn.initiate(player);
			}
			else {
				plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION).send();
				plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
			}
		}
		else {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_NO_BEDSPAWN).send();
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
		}
	}

	@Override
	public void execute(final Player player, final Destination finalDestination, final ItemStack playerItem, final MessageId messageId) {
		new TeleportExecutor(plugin, warmupMap).execute(player, finalDestination, playerItem, messageId);
	}

}
