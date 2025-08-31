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
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


final class TeleportSubcommand extends AbstractSubcommand
{
	TeleportSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "teleport";
		this.aliases = Set.of("tp");
		this.permissionNode = "lodestar.teleport";
		this.usageString = "/lodestar teleport <destination name>";
		this.description = MessageId.COMMAND_HELP_TELEPORT;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args)
	{
		if (args.length == 2)
		{
			return ctx.datastore().destinations().names().stream()
					.filter(string -> matchPrefix(string, args[1]))
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_TELEPORT).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check for in game player
		if (!(sender instanceof Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// join remaining arguments to get destination name
		String destinationName = String.join(" ", args);

		// test that destination name is valid
		if (!ctx.lodeStarUtility().destinationExists(destinationName))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get destination from datastore
		Destination destination = ctx.datastore().destinations().get(destinationName);

		if (destination instanceof ValidDestination validDestination && validDestination.location() != null)
		{
			Location location = validDestination.location().toBukkitLocation();
			assert location != null;

			ctx.soundConfig().playSound(player.getLocation(), SoundId.TELEPORT_SUCCESS_DEPARTURE);
			player.teleport(location);
			ctx.messageBuilder().compose(sender, MessageId.TELEPORT_SUCCESS)
					.setMacro(Macro.DESTINATION, validDestination)
					.send();
			ctx.soundConfig().playSound(location, SoundId.TELEPORT_SUCCESS_ARRIVAL);
			return true;
		}
		else
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION).send();
			ctx.soundConfig().playSound(sender, SoundId.TELEPORT_DENIED_WORLD_DISABLED);
		}

		return true;
	}

}
