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

package com.winterhavenmc.lodestar.adapters.teleporter.bukkit;

import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.InvalidDestination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.plugin.util.TeleportCtx;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import org.bukkit.entity.Player;


final class DestinationTeleporter extends AbstractTeleporter implements Teleporter
{
	/**
	 * Class constructor
	 *
	 * @param teleportExecutor the teleport executor
	 */
	DestinationTeleporter(final TeleportCtx ctx, final TeleportExecutor teleportExecutor)
	{
		super(ctx, teleportExecutor);
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
		final String key = ctx.lodeStarUtility().getDestinationKey(player.getInventory().getItemInMainHand());

		// execute teleport or send invalid destination message
		Destination destination = ctx.datastore().destinations().get(key);

		switch (destination)
		{
			case ValidDestination validDestination -> execute(player, validDestination, MessageId.EVENT_TELEPORT_WARMUP_DESTINATION);
			case InvalidDestination ignored -> sendInvalidDestinationMessage(player, key);
		}
	}

}
