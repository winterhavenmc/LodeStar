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

import org.bukkit.entity.Player;


final class HomeTeleporter extends AbstractTeleporter {


	/**
	 * Class constructor
	 *
	 * @param plugin the player to teleport
	 * @param teleportExecutor the teleport executor
	 */
	HomeTeleporter(final PluginMain plugin, final TeleportExecutor teleportExecutor) {
		super(plugin, teleportExecutor);
	}


	/**
	 * Begin teleport to players bedspawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player) {
		getHomeDestination(player).ifPresentOrElse(
				destination -> execute(player, destination, MessageId.TELEPORT_WARMUP),
				() -> fallbackToSpawn(player)
		);
	}


	/**
	 * Initiate fallback teleport to spawn if configured
	 *
	 * @param player the player to teleport
	 */
	void fallbackToSpawn(final Player player) {
		if (plugin.getConfig().getBoolean("bedspawn-fallback")) {
			getSpawnDestination(player).ifPresentOrElse(
					destination -> new SpawnTeleporter(plugin, teleportExecutor).initiate(player),
					() -> sendInvalidDestinationMessage(player, plugin.messageBuilder.getHomeDisplayName().orElse("Home"))
			);
		}
		else {
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_NO_BEDSPAWN).send();
			plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
		}
	}

}
