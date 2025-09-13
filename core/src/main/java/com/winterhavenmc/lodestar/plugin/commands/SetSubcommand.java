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

import com.winterhavenmc.lodestar.models.destination.*;
import com.winterhavenmc.lodestar.models.location.ImmutableLocation;
import com.winterhavenmc.lodestar.models.location.ValidLocation;
import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

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
		this.description = MessageId.COMMAND_SUCCESS_HELP_SET;
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
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_PERMISSION_DENIED).send();
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

		//TODO: get validDestination to use destination subfields in message

		// check if validDestination name is a reserved name
		if (ctx.lodeStarUtility().isRerservedName(destinationName))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_RESERVED)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			return true;
		}

		// get Destination from data store
		Destination destination = ctx.datastore().destinations().get(destinationName);

		// check for overwrite permission if validDestination already exists TODO: shouldn't this check negate permission?
		if (destination instanceof ValidDestination && sender.hasPermission(permissionNode + ".overwrite"))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_OVERWRITE_PERMISSION_DENIED)
					.setMacro(Macro.DESTINATION, destination)
					.send();
			return true;
		}

		// send warning message if name begins with a number
//		if (ctx.lodeStarUtility().deriveKey(destinationName).matches("^\\d*_.*"))
//		{
//			ctx.messageBuilder().compose(sender, MessageId.COMMAND_WARN_SET_NUMERIC_PREFIX)
//					.setMacro(Macro.DESTINATION, destinationName)
//					.send();
//		}

		// create validDestination object
		if (ImmutableLocation.of(location) instanceof ValidLocation validLocation)
		{
			Destination newDestination = StoredDestination.of(destinationName, validLocation);

			switch (newDestination)
			{
				case StoredDestination storedDestination -> sendSuccessMessage(sender, storedDestination);
				case HomeDestination homeDestination -> sendFailHomeMessage(sender, homeDestination);
				case SpawnDestination spawnDestination -> sendFailSpawnMessage(sender, spawnDestination);
				case InvalidDestination invalidDestination -> sendFailInvalidMessage(sender, invalidDestination);
				default -> throw new IllegalStateException("Unexpected value: " + newDestination);
			}
		}
		return true;
	}


	private void sendSuccessMessage(CommandSender sender, StoredDestination storedDestination)
	{
		ctx.datastore().destinations().save(Collections.singleton(storedDestination));
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_SET)
				.setMacro(Macro.DESTINATION, storedDestination)
				.send();
	}


	private void sendFailHomeMessage(CommandSender sender, HomeDestination homeDestination)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_RESERVED)
				.setMacro(Macro.DESTINATION, homeDestination)
				.send();
	}


	private void sendFailSpawnMessage(CommandSender sender, SpawnDestination spawnDestination)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_RESERVED)
				.setMacro(Macro.DESTINATION, spawnDestination)
				.send();
	}


	private void sendFailInvalidMessage(CommandSender sender, InvalidDestination invalidDestination)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_REASON)
				.setMacro(Macro.DESTINATION, invalidDestination.displayName())
				.setMacro(Macro.FAIL_REASON, invalidDestination.reason())
				.send();
	}

}
