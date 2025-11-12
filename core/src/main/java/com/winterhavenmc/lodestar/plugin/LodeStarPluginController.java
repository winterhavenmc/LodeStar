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

package com.winterhavenmc.lodestar.plugin;

import com.winterhavenmc.lodestar.plugin.ports.commands.CommandManager;
import com.winterhavenmc.lodestar.plugin.ports.listeners.PlayerInteractEventListener;
import com.winterhavenmc.lodestar.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.plugin.ports.listeners.PlayerEventListener;
import com.winterhavenmc.lodestar.plugin.ports.teleporter.TeleportHandler;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to create items that return player to a stored location when clicked.<br>
 * An alternative to the /warp command.
 *
 * @author Tim Savage
 */
public final class LodeStarPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public ConnectionProvider datastore;
	public TeleportHandler teleportHandler;
	public CommandManager commandManager;
	public LodeStarUtility lodeStarUtility;
	public PlayerEventListener playerEventListener;
	public PlayerInteractEventListener playerInteractEventListener;


	@Override
	public void startUp(final JavaPlugin plugin,
	                    final ConnectionProvider connectionProvider,
						final CommandManager commandManager,
						final TeleportHandler teleportHandler,
	                    final PlayerEventListener playerEventListener,
	                    final PlayerInteractEventListener playerInteractEventListener)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		this.messageBuilder = MessageBuilder.create(plugin);

		// get initialized destination storage object
		this.datastore = connectionProvider.connect();

		// instantiate lodestar factory
		this.lodeStarUtility = new LodeStarUtility(plugin, messageBuilder, datastore);

		// instantiate metrics handler
		new MetricsHandler(plugin);

		// instantiate context containers
		CommandContextContainer commandCtx = new CommandContextContainer(plugin, messageBuilder, datastore, lodeStarUtility);
		ListenerContextContainer listenerCtx = new ListenerContextContainer(plugin, messageBuilder);
		TeleporterContextContainer teleporterCtx = new TeleporterContextContainer(plugin, messageBuilder, datastore, lodeStarUtility);

		// instantiate teleport manager
		this.teleportHandler = teleportHandler.init(teleporterCtx);

		// instantiate command manager
		this.commandManager = commandManager.init(commandCtx);

		// instantiate event listeners
		this.playerEventListener = playerEventListener.init(teleportHandler, teleporterCtx);
		this.playerInteractEventListener = playerInteractEventListener.init(teleportHandler, listenerCtx);
	}


	@Override
	public void shutDown()
	{
		datastore.close();
	}


	public record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                      ConnectionProvider datastore, LodeStarUtility lodeStarUtility) { }


	public record ListenerContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder) { }


	public record TeleporterContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                         ConnectionProvider datastore, LodeStarUtility lodeStarUtility) { }
}
