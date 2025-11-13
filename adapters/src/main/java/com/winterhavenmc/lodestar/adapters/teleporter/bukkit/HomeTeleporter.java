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

import com.winterhavenmc.lodestar.models.destination.*;
import com.winterhavenmc.lodestar.models.location.ConfirmedLocation;
import com.winterhavenmc.lodestar.models.location.ValidLocation;
import com.winterhavenmc.lodestar.plugin.util.TeleportCtx;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.util.SoundId;

import org.bukkit.entity.Player;


final class HomeTeleporter extends AbstractTeleporter implements Teleporter
{
	/**
	 * Class constructor
	 *
	 * @param teleportExecutor the teleport executor
	 */
	HomeTeleporter(final TeleportCtx ctx, final TeleportExecutor teleportExecutor)
	{
		super(ctx, teleportExecutor);
	}


	/**
	 * Begin teleport to players bedspawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player)
	{
		if (getHomeDestination(player) instanceof ValidDestination validDestination
				&& ConfirmedLocation.of(player.getRespawnLocation()) instanceof ValidLocation validLocation)
		{
			switch (TeleportDestination.of(validDestination, validLocation))
			{
				case ValidDestination destination -> execute(player, destination, MessageId.EVENT_TELEPORT_WARMUP_DESTINATION);
				case InvalidDestination ignored -> fallbackToSpawn(player);
			}
		}
	}


	/**
	 * Initiate fallback teleport to spawn if configured
	 *
	 * @param player the player to teleport
	 */
	private void fallbackToSpawn(final Player player)
	{
		if (ctx.plugin().getConfig().getBoolean("bedspawn-fallback"))
		{
			SpawnTeleporter spawnTeleporter = new SpawnTeleporter(ctx, teleportExecutor);

			switch (getSpawnDestination(player))
			{
				case ValidDestination ignored -> spawnTeleporter.initiate(player);
				case InvalidDestination ignored -> sendInvalidDestinationMessage(player,
						ctx.messageBuilder().constants().getString(LodeStarUtility.HOME_KEY).orElse("Home"));
			}
		}
		else
		{
			ctx.messageBuilder().compose(player, MessageId.EVENT_TELEPORT_FAIL_NO_BEDSPAWN).send();
			ctx.messageBuilder().sounds().play(player, SoundId.TELEPORT_CANCELLED);
		}
	}

}
