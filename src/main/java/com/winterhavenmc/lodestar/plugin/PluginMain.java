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
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
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
public final class PluginMain extends JavaPlugin
{
	public MessageBuilder<MessageId, Macro> messageBuilder;
	public DataStore dataStore;
	public TeleportHandler teleportHandler;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public CommandManager commandManager;
	public LodeStarUtility lodeStarUtility;


	@Override
	public void onEnable()
	{
		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate message builder
		messageBuilder = new MessageBuilder<>(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// get initialized destination storage object
		dataStore = DataStore.connect(this);

		// instantiate teleport manager
		teleportHandler = new TeleportHandler(this);

		// instantiate command manager
		commandManager = new CommandManager(this);

		// instantiate lodestar factory
		lodeStarUtility = new LodeStarUtility(this);

		// instantiate event listeners
		new PlayerEventListener(this);
		new PlayerInteractEventListener(this);

		// instantiate metrics handler
		new MetricsHandler(this);
	}


	@Override
	public void onDisable()
	{
		dataStore.close();
	}

}
