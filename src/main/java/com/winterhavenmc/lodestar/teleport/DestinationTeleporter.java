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
import com.winterhavenmc.lodestar.destination.Destination;
import com.winterhavenmc.lodestar.destination.InvalidDestination;
import com.winterhavenmc.lodestar.destination.ValidDestination;
import com.winterhavenmc.lodestar.messages.MessageId;
import org.bukkit.entity.Player;


final class DestinationTeleporter extends AbstractTeleporter implements Teleporter
{
	/**
	 * Class constructor
	 *
	 * @param plugin           the player to teleport
	 * @param teleportExecutor the teleport executor
	 */
	DestinationTeleporter(final PluginMain plugin, final TeleportExecutor teleportExecutor)
	{
		super(plugin, teleportExecutor);
	}


	/**
	 * Begin teleport to destination determined by LodeStar item key
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player)
	{
		// get item key from player item in main hand
		final String key = plugin.lodeStarUtility.getKey(player.getInventory().getItemInMainHand());

		// execute teleport or send invalid destination message
		Destination destination = plugin.dataStore.selectRecord(key);

		switch (destination)
		{
			case ValidDestination validDestination -> execute(player, validDestination, MessageId.TELEPORT_WARMUP);
			case InvalidDestination ignored -> sendInvalidDestinationMessage(player, key);
		}
	}

}
