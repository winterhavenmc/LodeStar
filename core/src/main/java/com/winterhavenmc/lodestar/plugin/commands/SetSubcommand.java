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

import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;
import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.InvalidDestination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


final class SetSubcommand extends AbstractSubcommand
{
	SetSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "set";
		this.permissionNode = "lodestar.set";
		this.usageString = "/lodestar set <destination_name>";
		this.description = MessageId.COMMAND_HELP_SET;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args)
	{
		if (args.length == 2)
		{
			Predicate<String> startsWith = string -> string.toLowerCase().startsWith(args[1].toLowerCase());

			return ctx.datastore().destinations().names().stream()
					.filter(startsWith)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// sender must be in game player
		if (!(sender instanceof Player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_SET).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// get player location
		Location location = ((Player) sender).getLocation();

		// set destinationName to passed argument
		String destinationName = String.join(" ", args);

		// check if validDestination name is a reserved name
		if (isRerservedName(destinationName))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_RESERVED)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get optional Destination from data store
		Destination destination = ctx.datastore().destinations().get(destinationName);

		// check for overwrite permission if validDestination already exists TODO: shouldn't this check negate permission?
		if (destination instanceof ValidDestination && sender.hasPermission(permissionNode + ".overwrite"))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_OVERWRITE)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// send warning message if name begins with a number
		if (ctx.lodeStarUtility().deriveKey(destinationName).matches("^\\d*_.*"))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_WARN_SET_NUMERIC_PREFIX)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
		}

		// create validDestination object
		switch (Destination.of(Destination.Type.STORED, destinationName, location))
		{
			case ValidDestination validDestination -> {
				ctx.datastore().destinations().save(Collections.singleton(validDestination));
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET)
						.setMacro(Macro.DESTINATION, validDestination.displayName())
						.setMacro(Macro.DESTINATION_LOCATION, validDestination.location())
						.send();
				ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
			}
			case InvalidDestination invalidDestination -> {
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_REASON)
						.setMacro(Macro.DESTINATION, invalidDestination.displayName())
						.setMacro(Macro.FAIL_REASON, invalidDestination.reason())
						.send();
				ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			}
		}

		return true;
	}

}
