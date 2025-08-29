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

package com.winterhavenmc.lodestar.plugin.commands;

import com.winterhavenmc.lodestar.plugin.PluginMain;
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


	BindSubcommand(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.name = "bind";
		this.permissionNode = "lodestar.bind";
		this.usageString = "/lodestar bind <destination name>";
		this.description = MessageId.COMMAND_HELP_BIND;
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
			List<String> destinationNames = new ArrayList<>(plugin.dataStore.destinations().names());
			destinationNames.addFirst(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"));
			destinationNames.addFirst(plugin.messageBuilder.getHomeDisplayName().orElse("Home"));
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
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check sender has permission
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_BIND).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check minimum arguments
		if (args.size() < getMinArgs())
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// join remaining arguments into destination name
		String suppliedName = String.join(" ", args);

		// check if destination exists
		if (!plugin.lodeStarUtility.destinationExists(suppliedName))
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, suppliedName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check that item in hand is valid material
		if (notRequiredDefaultItem(player) || notValidItem(player))
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_MATERIAL)
					.setMacro(Macro.DESTINATION, suppliedName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get formatted destination name
		String formattedName = plugin.lodeStarUtility.getDisplayName(suppliedName).orElse(suppliedName);

		// set destination in item lore
		plugin.lodeStarUtility.setMetaData(player.getInventory().getItemInMainHand(), formattedName);

		// send success message
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_BIND)
				.setMacro(Macro.DESTINATION, formattedName)
				.send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_BIND);

		return true;
	}

	private boolean notRequiredDefaultItem(final Player player)
	{
		return plugin.getConfig().getBoolean("default-material-only")
				&& !player.hasPermission("lodestar.default-override")
				&& !plugin.lodeStarUtility.isDefaultItem(player.getInventory().getItemInMainHand());
	}

	private boolean notValidItem(final Player player)
	{
		return invalidMaterials.contains(player.getInventory().getItemInMainHand().getType());
	}

}
