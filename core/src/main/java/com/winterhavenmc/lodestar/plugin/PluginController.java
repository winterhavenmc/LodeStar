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
import com.winterhavenmc.lodestar.plugin.storage.DataStore;
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
public final class PluginController
{
	public MessageBuilder messageBuilder;
	public DataStore datastore;
	public TeleportHandler teleportHandler;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public CommandManager commandManager;
//	public LodeStarUtility lodeStarUtility;


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
		datastore = DataStore.connect(plugin, connectionProvider);

		// instantiate lodestar factory
//		lodeStarUtility = new LodeStarUtility(messageBuilder);

		// create context container
		ContextContainer ctx = new ContextContainer(plugin, messageBuilder, soundConfig, worldManager, datastore);

		// instantiate teleport manager
		teleportHandler = new TeleportHandler(ctx);

		// instantiate command manager
		commandManager = new CommandManager(ctx);

		// instantiate event listeners
		new PlayerEventListener(teleportHandler, ctx);
		new PlayerInteractEventListener(teleportHandler, ctx);

		// instantiate metrics handler
		new MetricsHandler(plugin);
	}


	public void shutDown()
	{
		datastore.close();
	}


	public record ContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder, SoundConfiguration soundConfig,
	                               WorldManager worldManager, DataStore datastore) { }
}
