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

package com.winterhavenmc.lodestar.plugin.commands;

import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.util.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implements command executor for LodeStar commands.
 */
public final class CommandManager implements TabExecutor
{
	private final SubcommandRegistry subcommandRegistry = new SubcommandRegistry();
	private final LodeStarPluginController.CommandContextContainer ctx;


	/**
	 * constructor method for CommandManager class
	 */
	public CommandManager(final LodeStarPluginController.CommandContextContainer ctx)
	{
		this.ctx = ctx;

		// register this class as command executor
		Objects.requireNonNull(ctx.plugin().getCommand("lodestar")).setExecutor(this);

		// register subcommands
		for (SubcommandType subcommandType : SubcommandType.values())
		{
			subcommandRegistry.register(subcommandType.create(ctx));
		}

		// register help command
		subcommandRegistry.register(new HelpSubcommand(ctx, subcommandRegistry));
	}


	/**
	 * Tab completer for LodeStar
	 */
	@Override
	public List<String> onTabComplete(@Nonnull final CommandSender sender, @Nonnull final Command command,
	                                  @Nonnull final String alias, final String[] args)
	{
		// if more than one argument, use tab completer of subcommand
		if (args.length > 1)
		{
			// get subcommand from map
			Optional<Subcommand> optionalSubcommand = subcommandRegistry.getSubcommand(args[0]);

			// if no subcommand returned from map, return empty list
			if (optionalSubcommand.isEmpty())
			{
				return Collections.emptyList();
			}

			// unwrap optional subcommand
			Subcommand subcommand = optionalSubcommand.get();

			// return subcommand tab completer output
			return subcommand.onTabComplete(sender, command, alias, args);
		}

		// return list of subcommands for which sender has permission
		return getMatchingSubcommandNames(sender, args[0]);
	}


	/**
	 * command executor method for LodeStar
	 */
	@Override
	public boolean onCommand(@Nonnull final CommandSender sender, @Nonnull final Command cmd,
	                         @Nonnull final String label, final String[] args)
	{
		// convert args array to list
		List<String> argsList = new ArrayList<>(Arrays.asList(args));

		String subcommandName;

		// get subcommand, remove from front of list
		if (!argsList.isEmpty())
		{
			subcommandName = argsList.removeFirst();
		}

		// if no arguments, set command to help
		else
		{
			subcommandName = "help";
		}

		// get subcommand from map by name
		Optional<Subcommand> optionalSubcommand = subcommandRegistry.getSubcommand(subcommandName);

		// if subcommand is empty, get help command from map
		if (optionalSubcommand.isEmpty())
		{
			optionalSubcommand = subcommandRegistry.getSubcommand("help");
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_INVALID);
		}

		// execute subcommand
		optionalSubcommand.ifPresent(subcommand -> subcommand.onCommand(sender, argsList));

		return true;
	}


	/**
	 * Get matching list of subcommands for which sender has permission
	 *
	 * @param sender      the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	private List<String> getMatchingSubcommandNames(final CommandSender sender, final String matchString)
	{
		return subcommandRegistry.getKeys().stream()
				.map(subcommandRegistry::getSubcommand)
				.filter(Optional::isPresent)
				.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
				.map(subcommand -> subcommand.get().getName())
				.filter(name -> name.toLowerCase().startsWith(matchString.toLowerCase()))
				.collect(Collectors.toList());
	}

}
