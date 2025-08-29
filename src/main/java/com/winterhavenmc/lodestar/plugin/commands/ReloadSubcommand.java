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
import com.winterhavenmc.lodestar.plugin.messages.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;
import org.bukkit.command.CommandSender;

import java.util.List;


/**
 * Reload command implementation<br>
 * reloads plugin configuration
 */
final class ReloadSubcommand extends AbstractSubcommand
{
	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class instance
	 */
	ReloadSubcommand(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.name = "reload";
		this.permissionNode = "lodestar.reload";
		this.usageString = "/lodestar reload";
		this.description = MessageId.COMMAND_HELP_RELOAD;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_RELOAD).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (args.size() > getMaxArgs())
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// reinstall main configuration if necessary
		plugin.saveDefaultConfig();

		// reload main configuration
		plugin.reloadConfig();

		// reload enabled worlds list
		plugin.worldManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload messages
		plugin.messageBuilder.reload();

		// send reloaded message
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();

		return true;
	}

}
