package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.util.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.winterhaven_mc.lodestar.messages.MessageId.*;


/**
 * Status command implementation<br>
 * displays plugin settings
 */
public class StatusCommand extends AbstractCommand {

	private final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class instance
	 */
	StatusCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("status");
		this.setUsage("/lodestar status");
		this.setDescription(COMMAND_HELP_STATUS);
		this.setMaxArgs(0);
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission("lodestar.status")) {
			Message.create(sender, PERMISSION_DENIED_STATUS).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (args.size() > getMaxArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// output config settings
		String versionString = plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Storage type: "
				+ ChatColor.RESET + plugin.dataStore.getName());

		sender.sendMessage(ChatColor.GREEN + "Default material: "
				+ ChatColor.RESET + plugin.getConfig().getString("default-material"));

		sender.sendMessage(ChatColor.GREEN + "Minimum distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("minimum-distance"));

		sender.sendMessage(ChatColor.GREEN + "Warmup: "
				+ ChatColor.RESET
				+ LanguageManager.getInstance().getTimeString(TimeUnit.SECONDS.toMillis(
				plugin.getConfig().getInt("teleport-warmup"))));

		sender.sendMessage(ChatColor.GREEN + "Cooldown: "
				+ ChatColor.RESET
				+ LanguageManager.getInstance().getTimeString(TimeUnit.SECONDS.toMillis(
				plugin.getConfig().getInt("teleport-cooldown"))));

		sender.sendMessage(ChatColor.GREEN + "Shift-click required: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("shift-click"));

		sender.sendMessage(ChatColor.GREEN + "Cancel on damage/movement/interaction: "
				+ ChatColor.RESET + "[ "
				+ plugin.getConfig().getBoolean("cancel-on-damage") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-movement") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-interaction") + " ]");

		sender.sendMessage(ChatColor.GREEN + "Remove from inventory: "
				+ ChatColor.RESET + plugin.getConfig().getString("remove-from-inventory"));

		sender.sendMessage(ChatColor.GREEN + "Allow in recipes: " + ChatColor.RESET
				+ plugin.getConfig().getBoolean("allow-in-recipes"));

		sender.sendMessage(ChatColor.GREEN + "From nether: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-nether"));

		sender.sendMessage(ChatColor.GREEN + "From end: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-end"));

		sender.sendMessage(ChatColor.GREEN + "Lightning: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("lightning"));

		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());

		return true;
	}
}
