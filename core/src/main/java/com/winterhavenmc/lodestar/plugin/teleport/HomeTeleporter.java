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

import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.models.destination.InvalidDestination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

import org.bukkit.entity.Player;


final class HomeTeleporter extends AbstractTeleporter implements Teleporter
{
	/**
	 * Class constructor
	 *
	 * @param teleportExecutor the teleport executor
	 */
	HomeTeleporter(final PluginController.ContextContainer ctx, final TeleportExecutor teleportExecutor) {
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
		switch (getHomeDestination(player))
		{
			case ValidDestination validDestination -> execute(player, validDestination, MessageId.TELEPORT_WARMUP);
			case InvalidDestination ignored -> fallbackToSpawn(player);
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
						ctx.messageBuilder().getHomeDisplayName().orElse("Home"));
			}
		}
		else
		{
			ctx.messageBuilder().compose(player, MessageId.TELEPORT_FAIL_NO_BEDSPAWN).send();
			ctx.soundConfig().playSound(player, SoundId.TELEPORT_CANCELLED);
		}
	}

}
