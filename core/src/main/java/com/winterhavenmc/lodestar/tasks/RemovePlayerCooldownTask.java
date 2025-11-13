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

package com.winterhavenmc.lodestar.tasks;

import com.winterhavenmc.lodestar.ports.teleporter.TeleportHandler;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class RemovePlayerCooldownTask extends BukkitRunnable
{
	private final Player player;
	private final TeleportHandler teleportHandler;


	public RemovePlayerCooldownTask(final Player player, final TeleportHandler teleportHandler)
	{
		this.player = player;
		this.teleportHandler = teleportHandler;
	}


	@Override
	public void run()
	{
		teleportHandler.cancelPlayerCooldown(player);
	}
}
