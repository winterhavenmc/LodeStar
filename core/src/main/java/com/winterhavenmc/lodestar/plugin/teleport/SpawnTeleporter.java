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

package com.winterhavenmc.lodestar.plugin.teleport;

import com.winterhavenmc.lodestar.models.destination.InvalidDestination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import org.bukkit.entity.Player;


final class SpawnTeleporter extends AbstractTeleporter implements Teleporter
{
	/**
	 * Class constructor
	 *
	 * @param teleportExecutor the teleport executor
	 */
	SpawnTeleporter(final PluginController.ContextContainer ctx, final TeleportExecutor teleportExecutor)
	{
		super(ctx, teleportExecutor);
	}


	/**
	 * Begin teleport to world spawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player)
	{
		switch (getSpawnDestination(player))
		{
			case ValidDestination validDestination -> execute(player, validDestination, MessageId.TELEPORT_WARMUP_SPAWN);
			case InvalidDestination ignored -> sendInvalidDestinationMessage(player,
					ctx.messageBuilder().constants().getString(LodeStarUtility.SPAWN_KEY).orElse("Spawn"));
		}
	}

}
