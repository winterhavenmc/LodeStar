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

package com.winterhavenmc.lodestar.adapters.commands.bukkit;

import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.util.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


final class HelpSubcommand extends AbstractSubcommand
{
	private final SubcommandRegistry subcommandRegistry;


	/**
	 * Class constructor
	 *
	 * @param subcommandRegistry reference to subcommand map
	 */
	HelpSubcommand(final LodeStarPluginController.CommandContextContainer ctx, final SubcommandRegistry subcommandRegistry)
	{
		this.ctx = ctx;
		this.subcommandRegistry = subcommandRegistry;
		this.name = "help";
		this.permissionNode = "lodestar.help";
		this.usageString = "/lodestar help [command]";
		this.description = MessageId.COMMAND_SUCCESS_HELP_HELP;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args)
	{
		if (args.length == 2 && args[0].equalsIgnoreCase(this.name))
		{
			return subcommandRegistry.getKeys().stream()
					.map(subcommandRegistry::getSubcommand)
					.filter(Optional::isPresent)
					.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
					.map(subcommand -> subcommand.get().getName())
					.filter(subCommandName -> subCommandName.toLowerCase().startsWith(args[1].toLowerCase()))
					.filter(subCommandName -> !subCommandName.equalsIgnoreCase(this.name))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_HELP_PERMISSION_DENIED).send();
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.isEmpty())
		{
			displayUsageAll(sender);
			return true;
		}

		// display subcommand help message or invalid command message
		subcommandRegistry.getSubcommand(args.getFirst()).ifPresentOrElse(
				subcommand -> sendCommandHelpMessage(sender, subcommand),
				() -> sendCommandInvalidMessage(sender)
		);

		return true;
	}


	/**
	 * Send help description for subcommand to command sender with subcommand permission node,
	 * otherwise send invalid command message
	 *
	 * @param sender     the command sender
	 * @param subcommand the subcommand to display help description
	 */
	private void sendCommandHelpMessage(CommandSender sender, Subcommand subcommand)
	{
		if (sender.hasPermission(subcommand.getPermissionNode()))
		{
			ctx.messageBuilder().compose(sender, subcommand.getDescription()).send();
			subcommand.displayUsage(sender);
		}
		else
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_HELP_INVALID_COMMAND).send();
			ctx.messageBuilder().sounds().play(sender, SoundId.COMMAND_INVALID);
		}
	}


	/**
	 * Send invalid subcommand message to command sender and display usage for all subcommands
	 *
	 * @param sender the command sender
	 */
	private void sendCommandInvalidMessage(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_HELP_INVALID_COMMAND).send();
		ctx.messageBuilder().sounds().play(sender, SoundId.COMMAND_INVALID);
		displayUsageAll(sender);
	}


	/**
	 * Display usage message for all commands
	 *
	 * @param sender the command sender
	 */
	void displayUsageAll(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_HELP_USAGE_HEADER).send();

		subcommandRegistry.getKeys().stream()
				.map(subcommandRegistry::getSubcommand)
				.filter(Optional::isPresent)
				.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
				.forEach(subcommand -> subcommand.get().displayUsage(sender));
	}

}
