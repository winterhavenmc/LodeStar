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

import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
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
	 */
	ReloadSubcommand(final LodeStarPluginController.CommandContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "reload";
		this.permissionNode = "lodestar.reload";
		this.usageString = "/lodestar reload";
		this.description = MessageId.COMMAND_SUCCESS_HELP_RELOAD;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_RELOAD_PERMISSION_DENIED).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (args.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// reinstall main configuration if necessary
		ctx.plugin().saveDefaultConfig();

		// reload main configuration
		ctx.plugin().reloadConfig();

		// reload enabled worlds list
		ctx.worldManager().reload();

		// reload sounds
		ctx.soundConfig().reload();

		// reload messages
		ctx.messageBuilder().reload();

		// send reloaded message
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();

		return true;
	}

}
