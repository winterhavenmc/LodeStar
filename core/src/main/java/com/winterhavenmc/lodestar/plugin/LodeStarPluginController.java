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

import com.winterhavenmc.lodestar.plugin.commands.CommandManager;
import com.winterhavenmc.lodestar.plugin.listeners.PlayerEventListener;
import com.winterhavenmc.lodestar.plugin.listeners.PlayerInteractEventListener;
import com.winterhavenmc.lodestar.plugin.ports.datastore.ConnectionProvider;
import com.winterhavenmc.lodestar.plugin.teleport.TeleportHandler;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;

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
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public CommandManager commandManager;
	public LodeStarUtility lodeStarUtility;


	@Override
	public void startUp(final JavaPlugin plugin, final ConnectionProvider connectionProvider)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		messageBuilder = MessageBuilder.create(plugin);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		worldManager = new WorldManager(plugin);

		// get initialized destination storage object
		datastore = connectionProvider.connect();

		// instantiate lodestar factory
		lodeStarUtility = new LodeStarUtility(plugin, messageBuilder, datastore);

		// instantiate metrics handler
		new MetricsHandler(plugin);

		// instantiate context containers
		CommandContextContainer commandCtx = new CommandContextContainer(plugin, messageBuilder, soundConfig, worldManager, datastore, lodeStarUtility);
		ListenerContextContainer listenerCtx = new ListenerContextContainer(plugin, messageBuilder, soundConfig);
		TeleporterContextContainer teleporterCtx = new TeleporterContextContainer(plugin, messageBuilder, soundConfig, worldManager, lodeStarUtility);

		// instantiate teleport manager
		teleportHandler = new TeleportHandler(teleporterCtx);

		// instantiate command manager
		commandManager = new CommandManager(commandCtx);

		// instantiate event listeners
		new PlayerEventListener(teleportHandler, listenerCtx);
		new PlayerInteractEventListener(teleportHandler, listenerCtx);
	}


	@Override
	public void shutDown()
	{
		datastore.close();
	}


	public record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                      SoundConfiguration soundConfig, WorldManager worldManager,
	                                      ConnectionProvider datastore, LodeStarUtility lodeStarUtility) { }


	public record ListenerContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                       SoundConfiguration soundConfig) { }


	public record TeleporterContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                         SoundConfiguration soundConfiguration, WorldManager worldManager,
	                                         LodeStarUtility lodeStarUtility) { }
}
