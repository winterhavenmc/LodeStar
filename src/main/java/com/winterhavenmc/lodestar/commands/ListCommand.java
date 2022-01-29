package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;

import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;


final class ListCommand extends SubcommandAbstract {

	private final PluginMain plugin;


	ListCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "list";
		this.usageString = "/lodestar list [page]";
		this.description = MessageId.COMMAND_HELP_LIST;
		this.maxArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to list destinations, output error message and return true
		if (!sender.hasPermission("lodestar.list")) {
			plugin.messageBuilder.build(sender, MessageId.PERMISSION_DENIED_LIST).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		if (args.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		int page = 1;

		if (args.size() == 1) {
			try {
				page = Integer.parseInt(args.get(0));
			}
			catch (NumberFormatException e) {
				// second argument not a page number, let default of 1 stand
			}
		}

		// set page to at least 1
		page = Math.max(1, page);

		// get configured items per page
		int itemsPerPage = plugin.getConfig().getInt("list-page-size");

		// get all records from datastore
		final List<String> allKeys = plugin.dataStore.selectAllKeys();

		if (plugin.getConfig().getBoolean("debug")) {
			plugin.getLogger().info("Total records fetched from db: " + allKeys.size());
		}

		// if display list is empty, output list empty message and return
		if (allKeys.isEmpty()) {
			plugin.messageBuilder.build(sender, MessageId.LIST_EMPTY).send();
			return true;
		}

		// get page count
		int pageCount = ((allKeys.size() - 1) / itemsPerPage) + 1;
		if (page > pageCount) {
			page = pageCount;
		}

		// get item range for page
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), allKeys.size());

		// get keys for items on page
		List<String> displayKeys = allKeys.subList(startIndex, endIndex);

		// display list header
		plugin.messageBuilder.build(sender, MessageId.LIST_HEADER).setMacro(Macro.PAGE_NUMBER, page).setMacro(Macro.PAGE_TOTAL, pageCount).send();

		int itemNumber = startIndex;

		for (String key : displayKeys) {

			Destination destination = plugin.dataStore.selectRecord(key);

			// increment item number
			itemNumber++;

			if (destination.isWorldValid()) {
				plugin.messageBuilder.build(sender, MessageId.LIST_ITEM)
						.setMacro(Macro.DESTINATION, destination.getDisplayName())
						.setMacro(Macro.ITEM_NUMBER, itemNumber)
						.setMacro(Macro.LOCATION, destination.getLocation())
						.send();
			}
			else {
				plugin.messageBuilder.build(sender, MessageId.LIST_ITEM_INVALID)
						.setMacro(Macro.DESTINATION, destination.getDisplayName())
						.setMacro(Macro.ITEM_NUMBER, itemNumber)
						.setMacro(Macro.WORLD, destination.getWorldName())
						.send();
			}
		}

		// display list footer
		plugin.messageBuilder.build(sender, MessageId.LIST_FOOTER).setMacro(Macro.PAGE_NUMBER, page).setMacro(Macro.PAGE_TOTAL, pageCount).send();

		return true;
	}

}
