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

import com.winterhavenmc.lodestar.models.destination.StoredDestination;
import com.winterhavenmc.lodestar.plugin.util.CommandCtx;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.InvalidDestination;

import org.bukkit.command.CommandSender;

import java.util.List;


final class ListSubcommand extends AbstractSubcommand
{
	ListSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "list";
		this.permissionNode = "lodestar.list";
		this.usageString = "/lodestar list [page]";
		this.description = MessageId.COMMAND_SUCCESS_HELP_LIST;
		this.maxArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to list destinations, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_LIST_PERMISSION_DENIED).send();
			return true;
		}

		if (args.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
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
		int itemsPerPage = ctx.plugin().getConfig().getInt("list-page-size");

		// get all records from datastore
		final List<String> destinationNames = ctx.datastore().destinations().names();

		if (ctx.plugin().getConfig().getBoolean("debug"))
		{
			ctx.plugin().getLogger().info("Total records fetched from data store: " + destinationNames.size());
		}

		// if display list is empty, output list empty message and return
		if (destinationNames.isEmpty())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_EMPTY).send();
			return true;
		}

		// get page count
		int pageCount = ((destinationNames.size() - 1) / itemsPerPage) + 1;
		if (page > pageCount)
		{
			page = pageCount;
		}

		// get item range for page
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), destinationNames.size());

		// get keys for items on page
		List<String> displayKeys = destinationNames.subList(startIndex, endIndex);

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
			Destination destination = ctx.datastore().destinations().get(key);

			itemNumber++;

			switch (destination)
			{
				case StoredDestination storedDestination -> ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM_VALID)
						.setMacro(Macro.DESTINATION, storedDestination)
						.setMacro(Macro.ITEM_NUMBER, itemNumber)
						.send();

				case InvalidDestination invalidDestination -> ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_ITEM_INVALID)
						.setMacro(Macro.DESTINATION, invalidDestination)
						.setMacro(Macro.WORLD, "Invalid")
						.setMacro(Macro.ITEM_NUMBER, itemNumber)
						.send();

				default -> { }
			}
		}
	}


	private void displayListHeader(CommandSender sender, int page, int pageCount)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	private void displayListFooter(CommandSender sender, int page, int pageCount)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_LIST_FOOTER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}

}
