package com.winterhavenmc.lodestar;

import com.winterhavenmc.lodestar.commands.CommandManager;
import com.winterhavenmc.lodestar.listeners.PlayerEventListener;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.storage.DataStore;
import com.winterhavenmc.lodestar.teleport.TeleportManager;
import com.winterhavenmc.lodestar.util.LodeStarFactory;

import com.winterhavenmc.util.messagebuilder.MessageBuilder;
import com.winterhavenmc.util.soundconfig.SoundConfiguration;
import com.winterhavenmc.util.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.util.worldmanager.WorldManager;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


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
	public TeleportManager teleportManager;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public CommandManager commandManager;
	public PlayerEventListener playerEventListener;
	public LodeStarFactory lodeStarFactory;


	/**
	 * Constructor for mocking
	 */
	@SuppressWarnings("unused")
	public PluginMain() {
		super();
	}


	/**
	 * Constructor for mocking
	 */
	@SuppressWarnings("unused")
	private PluginMain(final JavaPluginLoader loader,
	                   final PluginDescriptionFile descriptionFile,
	                   final File dataFolder,
	                   final File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate message builder
		messageBuilder = new MessageBuilder<>(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// get initialized destination storage object
		dataStore = DataStore.connect(this);

		// instantiate teleport manager
		teleportManager = new TeleportManager(this);

		// instantiate command manager
		commandManager = new CommandManager(this);

		// instantiate player listener
		playerEventListener = new PlayerEventListener(this);

		// instantiate lodestar factory
		lodeStarFactory = new LodeStarFactory(this);
	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}

