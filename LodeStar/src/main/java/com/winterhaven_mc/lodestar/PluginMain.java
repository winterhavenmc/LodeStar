package com.winterhaven_mc.lodestar;

import com.winterhaven_mc.lodestar.commands.CommandManager;
import com.winterhaven_mc.lodestar.listeners.PlayerEventListener;
import com.winterhaven_mc.lodestar.storage.DataStore;
import com.winterhaven_mc.lodestar.storage.DataStoreFactory;
import com.winterhaven_mc.lodestar.teleport.TeleportManager;
import com.winterhaven_mc.lodestar.util.MessageManager;
import com.winterhaven_mc.util.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to create items that return player to
 * world spawn when clicked.<br>
 * An alternative to the /spawn command.
 * 
 * @author      Tim Savage
 * @version		1.0
 */
public final class PluginMain extends JavaPlugin {
	
	// static reference to main class
	public static PluginMain instance;

	public Boolean debug = getConfig().getBoolean("debug");
	
	public DataStore dataStore;
	public TeleportManager teleportManager;
	public MessageManager messageManager;
	public WorldManager worldManager;

	@Override
	public void onEnable() {

		// set static reference to main class
		instance = this;
		
		// install default config.yml if not present  
		saveDefaultConfig();
		
		// get initialized destination storage object
		dataStore = DataStoreFactory.create();

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate message manager
		messageManager = new MessageManager(this);
		
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

