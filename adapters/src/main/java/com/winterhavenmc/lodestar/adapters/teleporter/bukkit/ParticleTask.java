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

import com.winterhavenmc.lodestar.ports.teleporter.TeleportHandler;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * A self-cancelling, repeating task that generates ender signal particles
 * at a player's location as long as they are in the warmup hashmap
 *
 * @author savage
 */
final class ParticleTask extends BukkitRunnable
{
	private final Player player;
	private final TeleportHandler teleportHandler;


	/**
	 * Class constructor method
	 *
	 * @param player the player being teleported
	 */
	ParticleTask(final Player player, final TeleportHandler teleportHandler)
	{
		this.teleportHandler = teleportHandler;
		this.player = player;
	}


	@Override
	public void run()
	{
		// if player is in the warmup hashmap, display the particle effect at their location
		if (teleportHandler.isWarmingUp(player))
		{
			player.getWorld().playEffect(player.getLocation().add(0.0d, 1.0d, 0.0d), Effect.ENDER_SIGNAL, 0, 10);
		}
		// otherwise, cancel this repeating task if the player is not in the warmup hashmap
		else
		{
			this.cancel();
		}
	}

}
