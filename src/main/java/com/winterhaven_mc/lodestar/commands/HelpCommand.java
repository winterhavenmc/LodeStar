package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.MessageId.*;
import static com.winterhaven_mc.lodestar.sounds.SoundId.COMMAND_INVALID;


public class HelpCommand extends AbstractCommand {

	// reference to plugin main class instance
	private final PluginMain plugin;

	// reference to subcommand map
	private final SubcommandMap subcommandMap;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 * @param subcommandMap reference to subcommand map
	 */
	HelpCommand(final PluginMain plugin, final SubcommandMap subcommandMap) {
		this.plugin = Objects.requireNonNull(plugin);
		this.subcommandMap = Objects.requireNonNull(subcommandMap);
		this.setName("help");
		this.setUsage("/lodestar help [command]");
		this.setDescription(COMMAND_HELP_HELP);
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("help")) {
				for (String subcommand : subcommandMap.getKeys()) {
					if (sender.hasPermission("lodestar." + subcommand)
							&& subcommand.startsWith(args[1].toLowerCase())
							&& !subcommand.equalsIgnoreCase("help")) {
						returnList.add(subcommand);
					}
				}
			}
		}

		return returnList;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("lodestar.help")) {
			plugin.messageBuilder.build(sender, PERMISSION_DENIED_HELP).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.size() == 0) {
			displayUsageAll(sender);
			return true;
		}

		// get subcommand name
		String subcommandName = args.get(0);
		displayHelp(sender, subcommandName);
		return true;
	}

	/**
	 * Display help message and usage for a command
	 * @param sender the command sender
	 * @param commandName the name of the command for which to show help and usage
	 */
	void displayHelp(final CommandSender sender, final String commandName) {

		// get subcommand from map by name
		Subcommand subcommand = subcommandMap.getCommand(commandName);

		// if subcommand found in map, display help message and usage
		if (subcommand != null) {
			plugin.messageBuilder.build(sender, subcommand.getDescription()).send();
			subcommand.displayUsage(sender);
		}

		// else display invalid command help message and usage for all commands
		else {
			plugin.messageBuilder.build(sender, COMMAND_HELP_INVALID).send();
			plugin.soundConfig.playSound(sender, COMMAND_INVALID);
			displayUsageAll(sender);
		}
	}


	/**
	 * Display usage message for all commands
	 * @param sender the command sender
	 */
	void displayUsageAll(CommandSender sender) {

		plugin.messageBuilder.build(sender, COMMAND_HELP_USAGE_HEADER).send();

		for (String subcommandName : subcommandMap.getKeys()) {
			if (subcommandMap.getCommand(subcommandName) != null) {
				subcommandMap.getCommand(subcommandName).displayUsage(sender);
			}
		}
	}

}
