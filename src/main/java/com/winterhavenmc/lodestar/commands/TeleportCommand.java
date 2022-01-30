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
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


final class TeleportCommand extends SubcommandAbstract {

	private final PluginMain plugin;


	TeleportCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "teleport";
		this.usageString = "/lodestar teleport <destination name>";
		this.description = MessageId.COMMAND_HELP_TELEPORT;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		if (args.length == 2) {
			return plugin.dataStore.selectAllKeys();
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// check for permission
		if (!sender.hasPermission("lodestar.teleport")) {
			plugin.messageBuilder.build(sender, MessageId.PERMISSION_DENIED_TELEPORT).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check for in game player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs()) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// cast sender to player
		Player player = (Player) sender;

		// join remaining arguments to get destination name
		String destinationName = String.join(" ", args);

		// test that destination name is valid
		if (!Destination.exists(destinationName)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get destination from datastore
		Destination destination = plugin.dataStore.selectRecord(destinationName);

		if (destination != null && destination.getLocation() != null) {
			plugin.soundConfig.playSound(player.getLocation(), SoundId.TELEPORT_SUCCESS_DEPARTURE);
			player.teleport(destination.getLocation());
			plugin.messageBuilder.build(sender, MessageId.TELEPORT_SUCCESS)
					.setMacro(Macro.DESTINATION, destination)
					.send();
			plugin.soundConfig.playSound(destination.getLocation(), SoundId.TELEPORT_SUCCESS_ARRIVAL);
			return true;
		}
		else {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION).send();
			plugin.soundConfig.playSound(sender, SoundId.TELEPORT_DENIED_WORLD_DISABLED);
		}

		return true;
	}

}
