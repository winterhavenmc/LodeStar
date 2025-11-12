/*
 * Copyright (c) 2025 Tim Savage.
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

package com.winterhavenmc.lodestar.plugin;

import com.winterhavenmc.lodestar.plugin.ports.commands.CommandManager;
import com.winterhavenmc.lodestar.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.plugin.ports.listeners.PlayerEventListener;
import com.winterhavenmc.lodestar.plugin.ports.listeners.PlayerInteractEventListener;
import com.winterhavenmc.lodestar.plugin.ports.teleporter.TeleportHandler;
import org.bukkit.plugin.java.JavaPlugin;


public interface PluginController
{
	void startUp(JavaPlugin plugin, ConnectionProvider connectionProvider,
	             CommandManager commandManager,
				 TeleportHandler teleportHandler,
	             PlayerEventListener playerEventListener,
	             PlayerInteractEventListener playerInteractEventListener);

	void shutDown();
}
