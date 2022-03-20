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
import com.winterhavenmc.lodestar.messages.MessageId;

import com.winterhavenmc.lodestar.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static com.winterhavenmc.util.TimeUnit.SECONDS;


/**
 * Status command implementation<br>
 * displays plugin settings
 */
final class StatusSubcommand extends AbstractSubcommand {

	private final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class instance
	 */
	StatusSubcommand(final PluginMain plugin) {
		this.plugin = plugin;
		this.name = "status";
		this.permissionNode = "lodestar.status";
		this.usageString = "/lodestar status";
		this.description = MessageId.COMMAND_HELP_STATUS;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission(permissionNode)) {
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_STATUS).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (args.size() > getMaxArgs()) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// output config settings
		String versionString = plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);

		if (Config.DEBUG.isTrue()) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + Config.LANGUAGE.asOptionalString());

		sender.sendMessage(ChatColor.GREEN + "Default material: "
				+ ChatColor.RESET + Config.DEFAULT_MATERIAL.asOptionalString());

		sender.sendMessage(ChatColor.GREEN + "Minimum distance: "
				+ ChatColor.RESET + Config.MINIMUM_DISTANCE.asInt());

		sender.sendMessage(ChatColor.GREEN + "Warmup: "
				+ ChatColor.RESET
				+ plugin.messageBuilder.getTimeString(SECONDS.toMillis(Config.TELEPORT_WARMUP.asInt())));

		sender.sendMessage(ChatColor.GREEN + "Cooldown: "
				+ ChatColor.RESET
				+ plugin.messageBuilder.getTimeString(SECONDS.toMillis(Config.TELEPORT_COOLDOWN.asInt())));

		sender.sendMessage(ChatColor.GREEN + "Shift-click required: "
				+ ChatColor.RESET + Config.SHIFT_CLICK.isTrue());

		sender.sendMessage(ChatColor.GREEN + "Cancel on damage/movement/interaction: "
				+ ChatColor.RESET + "[ "
				+ Config.CANCEL_ON_DAMAGE.isTrue() + "/"
				+ Config.CANCEL_ON_MOVEMENT.isTrue() + "/"
				+ Config.CANCEL_ON_INTERACTION.isTrue() + " ]");

		sender.sendMessage(ChatColor.GREEN + "Remove from inventory: "
				+ ChatColor.RESET + Config.REMOVE_FROM_INVENTORY.asOptionalString());

		sender.sendMessage(ChatColor.GREEN + "Allow in recipes: " + ChatColor.RESET
				+ Config.ALLOW_IN_RECIPES.isTrue());

		sender.sendMessage(ChatColor.GREEN + "From nether: "
				+ ChatColor.RESET + Config.FROM_NETHER.isTrue());

		sender.sendMessage(ChatColor.GREEN + "From end: "
				+ ChatColor.RESET + Config.FROM_END.isTrue());

		sender.sendMessage(ChatColor.GREEN + "Lightning: "
				+ ChatColor.RESET + Config.LIGHTNING.isTrue());

		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());

		return true;
	}
}
