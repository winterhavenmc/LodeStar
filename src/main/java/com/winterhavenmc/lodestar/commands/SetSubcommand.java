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
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;

import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


final class SetSubcommand extends AbstractSubcommand {

	private final PluginMain plugin;


	SetSubcommand(final PluginMain plugin) {
		this.plugin = plugin;
		this.name = "set";
		this.permissionNode = "lodestar.set";
		this.usageString = "/lodestar set <destination_name>";
		this.description = MessageId.COMMAND_HELP_SET;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		if (args.length == 2) {

			Predicate<String> startsWith = string -> string.toLowerCase().startsWith(args[1].toLowerCase());

			return plugin.dataStore.selectAllKeys().stream()
					.filter(startsWith)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check for permission
		if (!sender.hasPermission(permissionNode)) {
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_SET).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs()) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// get player location
		Location location = ((Player) sender).getLocation();

		// set destinationName to passed argument
		String destinationName = String.join(" ", args);

		// check if destination name is a reserved name
		if (Destination.isReserved(destinationName)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_SET_RESERVED)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get optional destination from data store
		Optional<Destination> optionalDestination = plugin.dataStore.selectRecord(destinationName);

		// check for overwrite permission if destination already exists
		if (optionalDestination.isPresent() && sender.hasPermission(permissionNode + ".overwrite")) {
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_OVERWRITE)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// send warning message if name begins with a number
		if (plugin.lodeStarFactory.deriveKey(destinationName).matches("^\\d*_.*")) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_WARN_SET_NUMERIC_PREFIX)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
		}

		// create destination object
		Destination destination = new Destination(destinationName, location);

		// store destination object
		plugin.dataStore.insertRecord(destination);

		// send success message to player
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_SET).setMacro(Macro.DESTINATION, destinationName).send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);

		return true;
	}

}
