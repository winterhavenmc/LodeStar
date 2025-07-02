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
import com.winterhavenmc.lodestar.destination.Destination;
import com.winterhavenmc.lodestar.destination.InvalidDestination;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.destination.ValidDestination;
import org.bukkit.command.CommandSender;

import java.util.List;


final class ListSubcommand extends AbstractSubcommand
{
	ListSubcommand(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.name = "list";
		this.permissionNode = "lodestar.list";
		this.usageString = "/lodestar list [page]";
		this.description = MessageId.COMMAND_HELP_LIST;
		this.maxArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to list destinations, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_LIST).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (args.size() > getMaxArgs())
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		int page = 1;

		if (args.size() == 1)
		{
			try
			{
				page = Integer.parseInt(args.getFirst());
			} catch (NumberFormatException e)
			{
				// second argument not a page number, let default of 1 stand
			}
		}

		// set page to at least 1
		page = Math.max(1, page);

		// get configured items per page
		int itemsPerPage = plugin.getConfig().getInt("list-page-size");

		// get all records from datastore
		final List<String> allKeys = plugin.dataStore.selectAllKeys();

		if (plugin.getConfig().getBoolean("debug"))
		{
			plugin.getLogger().info("Total records fetched from data store: " + allKeys.size());
		}

		// if display list is empty, output list empty message and return
		if (allKeys.isEmpty())
		{
			plugin.messageBuilder.compose(sender, MessageId.LIST_EMPTY).send();
			return true;
		}

		// get page count
		int pageCount = ((allKeys.size() - 1) / itemsPerPage) + 1;
		if (page > pageCount)
		{
			page = pageCount;
		}

		// get item range for page
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), allKeys.size());

		// get keys for items on page
		List<String> displayKeys = allKeys.subList(startIndex, endIndex);

		displayListHeader(sender, page, pageCount);
		displayListItems(sender, startIndex, displayKeys);
		displayListFooter(sender, page, pageCount);

		return true;
	}

	private void displayListItems(CommandSender sender, int startIndex, List<String> displayKeys)
	{
		int itemNumber = startIndex;

		for (String key : displayKeys)
		{
			Destination destination = plugin.dataStore.selectRecord(key);

			itemNumber++;

			switch (destination)
			{
				case ValidDestination validDestination -> plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM)
						.setMacro(Macro.DESTINATION, validDestination.displayName())
						.setMacro(Macro.DESTINATION_LOCATION, validDestination.location())
						.setMacro(Macro.ITEM_NUMBER, itemNumber)
						.send();

				case InvalidDestination invalidDestination -> plugin.messageBuilder.compose(sender, MessageId.LIST_ITEM_INVALID)
						.setMacro(Macro.DESTINATION, invalidDestination.displayName())
						.setMacro(Macro.DESTINATION_WORLD, "Invalid")
						.setMacro(Macro.ITEM_NUMBER, itemNumber)
						.send();
			}
		}
	}


	private void displayListHeader(CommandSender sender, int page, int pageCount)
	{
		plugin.messageBuilder.compose(sender, MessageId.LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	private void displayListFooter(CommandSender sender, int page, int pageCount)
	{
		plugin.messageBuilder.compose(sender, MessageId.LIST_FOOTER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}

}
