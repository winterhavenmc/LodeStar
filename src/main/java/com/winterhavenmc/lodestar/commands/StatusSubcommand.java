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
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static com.winterhavenmc.library.TimeUnit.SECONDS;


/**
 * Status command implementation<br>
 * displays plugin settings
 */
final class StatusSubcommand extends AbstractSubcommand
{
	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class instance
	 */
	StatusSubcommand(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.name = "status";
		this.permissionNode = "lodestar.status";
		this.usageString = "/lodestar status";
		this.description = MessageId.COMMAND_HELP_STATUS;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_STATUS).send();
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

		// output plugin info and config settings
		displayPluginVersion(sender);
		displayDebugSetting(sender);
		displayLanguageSetting(sender);
		displayDefaultMaterialSetting(sender);
		displayMinimumDistanceSetting(sender);
		displayTeleportWarmupSetting(sender);
		displayTeleportCooldownSetting(sender);
		displayShiftClickSetting(sender);
		displayTeleportCancelSetting(sender);
		displayRemoveFromInventorySetting(sender);
		displayAllowInRecipesSetting(sender);
		displayFromNetherSetting(sender);
		displayFromEndSetting(sender);
		displayLightningSetting(sender);
		displayEnabledWorlds(sender);

		return true;
	}


	private void displayPluginVersion(final CommandSender sender)
	{
		String versionString = plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);
	}


	private void displayDebugSetting(final CommandSender sender)
	{
		if (plugin.getConfig().getBoolean("debug"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
	}


	private void displayLanguageSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));
	}


	private void displayDefaultMaterialSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Default material: "
				+ ChatColor.RESET + plugin.getConfig().getString("default-material"));
	}


	private void displayMinimumDistanceSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Minimum distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("minimum-distance"));
	}


	private void displayTeleportWarmupSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Warmup: "
				+ ChatColor.RESET
				+ plugin.messageBuilder.getTimeString(SECONDS.toMillis(plugin.getConfig().getInt("teleport-warmup"))));
	}


	private void displayTeleportCooldownSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Cooldown: "
				+ ChatColor.RESET
				+ plugin.messageBuilder.getTimeString(SECONDS.toMillis(plugin.getConfig().getInt("teleport-cooldown"))));
	}


	private void displayShiftClickSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Shift-click required: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("shift-click"));
	}


	private void displayTeleportCancelSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Cancel on damage/movement/interaction: "
				+ ChatColor.RESET + "[ "
				+ plugin.getConfig().getBoolean("cancel-on-damage") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-movement") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-interaction") + " ]");
	}


	private void displayRemoveFromInventorySetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Remove from inventory: "
				+ ChatColor.RESET + plugin.getConfig().getString("remove-from-inventory"));
	}


	private void displayAllowInRecipesSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Allow in recipes: " + ChatColor.RESET
				+ plugin.getConfig().getBoolean("allow-in-recipes"));
	}


	private void displayFromNetherSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "From nether: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-nether"));
	}


	private void displayFromEndSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "From end: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-end"));
	}


	private void displayLightningSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Lightning: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("lightning"));
	}


	private void displayEnabledWorlds(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());
	}

}
