package com.winterhaven_mc.lodestar;

import com.winterhaven_mc.lodestar.commands.CommandManager;
import com.winterhaven_mc.lodestar.listeners.PlayerEventListener;
import com.winterhaven_mc.lodestar.storage.DataStore;
import com.winterhaven_mc.lodestar.teleport.TeleportManager;

import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.YamlSoundConfiguration;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to create items that return player to
 * world spawn when clicked.<br>
 * An alternative to the /spawn command.
 *
 * @author Tim Savage
 * @version 1.0
 */
public final class PluginMain extends JavaPlugin {

	// global debug field
	public Boolean debug = getConfig().getBoolean("debug");

	public DataStore dataStore;
	public TeleportManager teleportManager;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;


	@Override
	public void onEnable() {

		// install default config.yml if not present
		saveDefaultConfig();

		// initialize language manager
		LanguageManager.init();

		// get initialized destination storage object
		dataStore = DataStore.create();

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate teleport manager
		teleportManager = new TeleportManager(this);

		// instantiate command manager
		new CommandManager(this);

		// instantiate player listener
		new PlayerEventListener(this);
	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}

