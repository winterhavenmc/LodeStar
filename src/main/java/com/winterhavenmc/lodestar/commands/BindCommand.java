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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;


final class BindCommand extends SubcommandAbstract {

	private final PluginMain plugin;

	private final List<Material> invalidMaterials = new ArrayList<>(Arrays.asList(
				Material.AIR,
				Material.CAVE_AIR,
				Material.VOID_AIR ));


	BindCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "bind";
		this.usageString ="/lodestar bind <destination name>";
		this.description = MessageId.COMMAND_HELP_BIND;
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

		// command sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check sender has permission
		if (!sender.hasPermission("lodestar.bind")) {
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_BIND).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check minimum arguments
		if (args.size() < getMinArgs()) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// cast sender to player
		Player player = (Player) sender;

		// join remaining arguments into destination name
		String destinationName = String.join(" ", args);

		// test that destination exists
		if (!Destination.exists(destinationName)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if default-item-only configured true, check that item in hand has default material and data
		if (plugin.getConfig().getBoolean("default-material-only")
				&& !sender.hasPermission("lodestar.default-override")) {
			if (!plugin.lodeStarFactory.isDefaultItem(playerItem)) {
				plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_MATERIAL)
						.setMacro(Macro.DESTINATION, destinationName)
						.send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// check that item in hand is valid material
		if (invalidMaterials.contains(playerItem.getType())) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_MATERIAL)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// try to get formatted destination name from storage
		Optional<Destination> destination = plugin.dataStore.selectRecord(destinationName);
		if (destination.isPresent()) {
			destinationName = destination.get().getDisplayName();
		}

		// set destination in item lore
		plugin.lodeStarFactory.setMetaData(playerItem, destinationName);

		// send success message
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_BIND)
				.setMacro(Macro.DESTINATION, destinationName)
				.send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_BIND);

		return true;
	}

}
