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

package com.winterhavenmc.lodestar.adapters.commands.bukkit;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.lodestar.plugin.util.CommandCtx;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.List;


/**
 * Status command implementation<br>
 * displays plugin settings
 */
final class StatusSubcommand extends AbstractSubcommand
{
	final ConfigRepository configRepository;

	/**
	 * Class constructor
	 */
	StatusSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "status";
		this.permissionNode = "lodestar.status";
		this.usageString = "/lodestar status";
		this.description = MessageId.COMMAND_SUCCESS_HELP_STATUS;
		this.configRepository = BukkitConfigRepository.create(ctx.plugin());
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION_DENIED).send();
			return true;
		}

		// check max arguments
		if (args.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			return true;
		}

		// output plugin info and config settings
		displayStatusHeader(sender);
		displayPluginVersion(sender);
		displayDebugSetting(sender);
		displayLanguageSetting(sender);
		displayLocaleSetting(sender);
		displayTimezoneSetting(sender);
		displayDefaultMaterialSetting(sender);
		displayMinimumDistanceSetting(sender);
		displayTeleportWarmupSetting(sender);
		displayTeleportCooldownSetting(sender);
		displayShiftClickSetting(sender);
		displayCancelOnDamageSetting(sender);
		displayCancelOnMovementSetting(sender);
		displayCancelOnInteractionSetting(sender);
		displayRemoveFromInventorySetting(sender);
		displayAllowInRecipesSetting(sender);
		displayFromNetherSetting(sender);
		displayFromEndSetting(sender);
		displayLightningSetting(sender);
		displayEnabledWorlds(sender);
		displayStatusFooter(sender);

		return true;
	}


	private void displayStatusHeader(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_HEADER).send();
	}


	private void displayPluginVersion(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_PLUGIN_VERSION)
				.setMacro(Macro.SETTING, ctx.plugin().getDescription().getVersion())
				.send();
	}


	private void displayDebugSetting(final CommandSender sender)
	{
		if (ctx.plugin().getConfig().getBoolean("debug"))
		{
			Component component = Component.text("<dark_red>DEBUG: true</dark_red>");
			try (BukkitAudiences audiences = BukkitAudiences.create(ctx.plugin()))
			{
				audiences.sender(sender).sendMessage(component);
			}
		}
	}


	private void displayLanguageSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LANGUAGE)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("language"))
				.send();
	}


	private void displayLocaleSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.SETTING, configRepository.locale().toLanguageTag())
				.send();
	}


	private void displayTimezoneSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TIMEZONE)
				.setMacro(Macro.SETTING, configRepository.zoneId().getId())
				.send();
	}


	private void displayDefaultMaterialSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DEFAULT_MATERIAL)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("item-material"))
				.send();
	}


	private void displayMinimumDistanceSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_MINIMUM_DISTANCE)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("minimum-distance"))
				.send();
	}


	private void displayTeleportWarmupSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TELEPORT_WARMUP)
				.setMacro(Macro.SETTING, Duration.ofSeconds(ctx.plugin().getConfig().getInt("teleport-warmup")))
				.send();
	}


	private void displayTeleportCooldownSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TELEPORT_COOLDOWN)
				.setMacro(Macro.SETTING, Duration.ofSeconds(ctx.plugin().getConfig().getInt("teleport-cooldown")))
				.send();
	}


	private void displayShiftClickSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SHIFT_CLICK)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("shift-click"))
				.send();
	}


	private void displayCancelOnDamageSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_CANCEL_ON_DAMAGE)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("cancel-on-damage"))
				.send();
	}

	private void displayCancelOnMovementSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_CANCEL_ON_MOVEMENT)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("cancel-on-movement"))
				.send();
	}

	private void displayCancelOnInteractionSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_CANCEL_ON_INTERACTION)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("cancel-on-interaction"))
				.send();
	}


	private void displayRemoveFromInventorySetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_INVENTORY_REMOVAL)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("remove-from-inventory"))
				.send();
	}


	private void displayAllowInRecipesSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ALLOW_IN_RECIPES)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("allow-in-recipes"))
				.send();
	}


	private void displayLightningSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DISPLAY_LIGHTNING)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("lightning"))
				.send();
	}


	private void displayEnabledWorlds(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.SETTING, ctx.messageBuilder().worlds().enabledNames().toString())
				.send();
	}


	private void displayFromNetherSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FROM_NETHER)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("from-nether"))
				.send();
	}


	private void displayFromEndSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FROM_END)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("from-end"))
				.send();
	}

	private void displayStatusFooter(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FOOTER)
				.setMacro(Macro.URL, "https://github.com/winterhavenmc/LodeStar")
				.send();
	}

}
