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

package com.winterhavenmc.lodestar;

import com.winterhavenmc.lodestar.commands.CommandManager;
import com.winterhavenmc.lodestar.listeners.PlayerEventListener;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.storage.DataStore;
import com.winterhavenmc.lodestar.teleport.TeleportHandler;
import com.winterhavenmc.lodestar.util.LodeStarFactory;
import com.winterhavenmc.lodestar.util.MetricsHandler;

import com.winterhavenmc.util.messagebuilder.MessageBuilder;
import com.winterhavenmc.util.soundconfig.SoundConfiguration;
import com.winterhavenmc.util.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.util.worldmanager.WorldManager;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to create items that return player to
 * world spawn when clicked.<br>
 * An alternative to the /spawn command.
 *
 * @author Tim Savage
 */
public final class PluginMain extends JavaPlugin {

	public MessageBuilder<MessageId, Macro> messageBuilder;
	public DataStore dataStore;
	public TeleportHandler teleportHandler;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public CommandManager commandManager;
	public PlayerEventListener playerEventListener;
	public LodeStarFactory lodeStarFactory;


	@Override
	public void onEnable() {

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

		// instantiate player listener
		playerEventListener = new PlayerEventListener(this);

		// instantiate lodestar factory
		lodeStarFactory = new LodeStarFactory(this);

		// instantiate metrics handler
		new MetricsHandler(this);

	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}
