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
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


final class BindSubcommand extends AbstractSubcommand
{
	private final List<Material> invalidMaterials = new ArrayList<>(Arrays.asList(
			Material.AIR,
			Material.CAVE_AIR,
			Material.VOID_AIR));


	BindSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "bind";
		this.permissionNode = "lodestar.bind";
		this.usageString = "/lodestar bind <destination name>";
		this.description = MessageId.COMMAND_SUCCESS_HELP_BIND;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
	                                  final String alias,
	                                  final String[] args)
	{
		if (args.length == 2)
		{
			List<String> destinationNames = new ArrayList<>(ctx.datastore().destinations().names());
			destinationNames.addFirst(ctx.messageBuilder().constants().getString(LodeStarUtility.SPAWN_KEY).orElse("Spawn"));
			destinationNames.addFirst(ctx.messageBuilder().constants().getString(LodeStarUtility.HOME_KEY).orElse("Home"));
			return destinationNames.stream().filter(key -> matchPrefix(key, args[1])).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// command sender must be player
		if (!(sender instanceof Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check sender has permission
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_BIND_PERMISSION_DENIED).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check minimum arguments
		if (args.size() < getMinArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// join remaining arguments into destination name
		String suppliedName = String.join(" ", args);

		//TODO: get valid destination from supplied name, to use destination subfields in message

		// check if destination exists
		if (!ctx.lodeStarUtility().destinationExists(suppliedName))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, suppliedName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check that item in hand is valid material
		if (notRequiredDefaultItem(player) || notValidItem(player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_MATERIAL)
					.setMacro(Macro.DESTINATION, suppliedName)
					.send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get formatted destination name
		String formattedName = ctx.lodeStarUtility().getDisplayName(suppliedName).orElse(suppliedName);

		// set item metadata
		ctx.lodeStarUtility().setItemMetadata(player.getInventory().getItemInMainHand(), formattedName);
		ctx.lodeStarUtility().setPersistentDestination(player.getInventory().getItemInMainHand(), formattedName);

		// send success message
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_BIND)
				.setMacro(Macro.ITEM, player.getInventory().getItemInMainHand())
				.setMacro(Macro.DESTINATION, formattedName)
				.send();

		// play sound effect
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_BIND);

		return true;
	}


	private boolean notRequiredDefaultItem(final Player player)
	{
		return ctx.plugin().getConfig().getBoolean("default-material-only")
				&& !player.hasPermission("lodestar.default-override")
				&& !ctx.lodeStarUtility().isDefaultMaterial(player.getInventory().getItemInMainHand());
	}


	private boolean notValidItem(final Player player)
	{
		return invalidMaterials.contains(player.getInventory().getItemInMainHand().getType());
	}

}
