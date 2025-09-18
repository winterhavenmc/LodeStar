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

import com.winterhavenmc.lodestar.adapters.commands.bukkit.BukkitCommandManager;
import com.winterhavenmc.lodestar.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.lodestar.adapters.listeners.bukkit.BukkitPlayerEventListener;
import com.winterhavenmc.lodestar.adapters.listeners.bukkit.BukkitPlayerInteractEventListener;
import com.winterhavenmc.lodestar.plugin.ports.commands.CommandManager;
import com.winterhavenmc.lodestar.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.plugin.ports.listeners.PlayerEventListener;
import com.winterhavenmc.lodestar.plugin.ports.listeners.PlayerInteractEventListener;
import org.bukkit.plugin.java.JavaPlugin;


public final class Bootstrap extends JavaPlugin
{
	PluginController pluginController;
	ConnectionProvider connectionProvider;
	CommandManager commandManager;
	PlayerEventListener playerEventListener;
	PlayerInteractEventListener playerInteractEventListener;


	@Override
	public void onEnable()
	{
		pluginController = new LodeStarPluginController();
		connectionProvider = new SqliteConnectionProvider(this);
		commandManager = new BukkitCommandManager();
		playerEventListener = new BukkitPlayerEventListener();
		playerInteractEventListener = new BukkitPlayerInteractEventListener();
		pluginController.startUp(this, connectionProvider, commandManager, playerEventListener, playerInteractEventListener);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}

}
