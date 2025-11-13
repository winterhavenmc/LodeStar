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

package com.winterhavenmc.lodestar;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.lodestar.adapters.commands.bukkit.BukkitCommandManager;
import com.winterhavenmc.lodestar.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.lodestar.adapters.listeners.bukkit.BukkitPlayerEventListener;
import com.winterhavenmc.lodestar.adapters.listeners.bukkit.BukkitPlayerInteractEventListener;
import com.winterhavenmc.lodestar.adapters.teleporter.bukkit.BukkitTeleportHandler;

import com.winterhavenmc.lodestar.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.ports.teleporter.TeleportHandler;

import com.winterhavenmc.lodestar.util.LodeStarUtility;
import com.winterhavenmc.lodestar.util.MetricsHandler;
import org.bukkit.plugin.java.JavaPlugin;


public final class Bootstrap extends JavaPlugin
{
	private ConnectionProvider connectionProvider;


	@Override
	public void onEnable()
	{
		saveDefaultConfig();

		final MessageBuilder messageBuilder = MessageBuilder.create(this);
		this.connectionProvider = new SqliteConnectionProvider(this);
		final LodeStarUtility lodeStarUtility = new LodeStarUtility(this, messageBuilder, connectionProvider);
		final TeleportHandler teleportHandler = new BukkitTeleportHandler(this, messageBuilder, connectionProvider, lodeStarUtility);

		new BukkitPlayerEventListener(this, messageBuilder, connectionProvider, lodeStarUtility, teleportHandler);
		new BukkitCommandManager(this, messageBuilder, connectionProvider, lodeStarUtility);
		new BukkitPlayerInteractEventListener(this, messageBuilder, teleportHandler);
		new MetricsHandler(this);
	}


	@Override
	public void onDisable()
	{
		connectionProvider.close();
	}

}
