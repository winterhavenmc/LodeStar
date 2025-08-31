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

import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


final class DeleteSubcommand extends AbstractSubcommand
{
	DeleteSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "delete";
		this.aliases = Set.of("unset");
		this.permissionNode = "lodestar.delete";
		this.usageString = "/lodestar delete <destination name>";
		this.description = MessageId.COMMAND_HELP_DELETE;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args)
	{
		if (args.length == 2)
		{
			List<String> destinationNames = new ArrayList<>(ctx.datastore().destinations().names());
			destinationNames.addFirst(ctx.messageBuilder().getSpawnDisplayName().orElse("Spawn"));
			destinationNames.addFirst(ctx.messageBuilder().getHomeDisplayName().orElse("Home"));
			return destinationNames.stream().filter(key -> matchPrefix(key, args[1])).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check for permission
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_DELETE).send();
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

		// join remaining arguments into destination name
		String destinationName = String.join(" ", args);

		// get key for destination name
		String key = ctx.lodeStarUtility().deriveKey(destinationName);

		// test that destination name is not reserved name
		if (isRerservedName(destinationName))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_DELETE_RESERVED)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		}
		// if delete method returns valid destination, delete was successful
		else if (ctx.datastore().destinations().delete(key) instanceof ValidDestination)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_DELETE)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
		}
		else
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		}
		return true;
	}

}
