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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


final class DeleteCommand extends SubcommandAbstract {

	private final PluginMain plugin;


	DeleteCommand(final PluginMain plugin) {
		this.plugin = plugin;
		this.name ="delete";
		this.permissionNode = "lodestar.delete";
		this.usageString ="/lodestar delete <destination name>";
		this.description = MessageId.COMMAND_HELP_DELETE;
		this.minArgs = 1;
		this.aliases = Set.of("unset");
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
		if (!sender.hasPermission(permissionNode)) {
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_DELETE).send();
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

		// join remaining arguments into destination name
		String destinationName = String.join(" ", args);

		// get key for destination name
		String key = plugin.lodeStarFactory.deriveKey(destinationName);

		// test that destination name is not reserved name
		if (Destination.isReserved(destinationName)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_DELETE_RESERVED)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}
		// if delete method returns valid destination, delete was successful
		else if (plugin.dataStore.deleteRecord(key).isPresent()) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_DELETE)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
		}
		else {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}
		return true;
	}

}
