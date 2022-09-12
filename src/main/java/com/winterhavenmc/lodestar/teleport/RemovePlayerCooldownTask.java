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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RemovePlayerCooldownTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;


	public RemovePlayerCooldownTask(final PluginMain plugin, final Player player) {
		this.plugin = plugin;
		this.player = player;
	}


	@Override
	public void run() {
		plugin.teleportHandler.cancelPlayerCooldown(player);
	}

}
