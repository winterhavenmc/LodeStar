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

package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


final class HelpCommand extends SubcommandAbstract {

	// reference to plugin main class instance
	private final PluginMain plugin;

	// reference to subcommand map
	private final SubcommandRegistry subcommandRegistry;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class instance
	 * @param subcommandRegistry reference to subcommand map
	 */
	HelpCommand(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
		this.plugin = Objects.requireNonNull(plugin);
		this.subcommandRegistry = Objects.requireNonNull(subcommandRegistry);
		this.name = "help";
		this.usageString = "/lodestar help [command]";
		this.description = MessageId.COMMAND_HELP_HELP;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("help")) {
				for (String subcommand : subcommandRegistry.getKeys()) {
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
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_HELP).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.size() == 0) {
			displayUsageAll(sender);
			return true;
		}

		// display subcommand help message or invalid command message
		subcommandRegistry.getCommand(args.get(0)).ifPresentOrElse(
				subcommand -> sendCommandHelpMessage(sender, subcommand),
				() -> sendCommandInvalidMessage(sender)
		);

		return true;
	}


	/**
	 * Send help description for subcommand to command sender
	 *
	 * @param sender the command sender
	 * @param subcommand the subcommand to display help description
	 */
	private void sendCommandHelpMessage(CommandSender sender, Subcommand subcommand) {
		plugin.messageBuilder.compose(sender, subcommand.getDescription()).send();
		subcommand.displayUsage(sender);
	}


	/**
	 * Send invalid subcommand message to command sender
	 *
	 * @param sender the command sender
	 */
	private void sendCommandInvalidMessage(CommandSender sender) {
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_HELP_INVALID).send();
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_INVALID);
		displayUsageAll(sender);
	}


	/**
	 * Display usage message for all commands
	 * @param sender the command sender
	 */
	void displayUsageAll(final CommandSender sender) {

		plugin.messageBuilder.compose(sender, MessageId.COMMAND_HELP_USAGE_HEADER).send();

		for (String subcommandName : subcommandRegistry.getKeys()) {
			subcommandRegistry.getCommand(subcommandName).ifPresent(subcommand -> subcommand.displayUsage(sender));
		}
	}

}
