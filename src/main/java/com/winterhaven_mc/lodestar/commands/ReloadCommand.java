package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.DataStore;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.MessageId.*;


/**
 * Reload command implementation<br>
 * reloads plugin configuration
 */
public class ReloadCommand extends AbstractCommand {

	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 */
	ReloadCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("reload");
		this.setUsage("/lodestar reload");
		this.setDescription(COMMAND_HELP_RELOAD);
		this.setMaxArgs(0);
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission("lodestar.reload")) {
			plugin.messageBuilder.build(sender, PERMISSION_DENIED_RELOAD).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (args.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// reinstall main configuration if necessary
		plugin.saveDefaultConfig();

		// reload main configuration
		plugin.reloadConfig();

		// update enabledWorlds list
		plugin.worldManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload messages
		plugin.messageBuilder.reload();

		// reload datastore
		DataStore.reload(plugin);

		// send reloaded message
		plugin.messageBuilder.build(sender, COMMAND_SUCCESS_RELOAD).send();

		return true;
	}

}
