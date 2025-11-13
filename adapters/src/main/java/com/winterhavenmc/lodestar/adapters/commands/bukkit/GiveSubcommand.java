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

import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.plugin.util.CommandCtx;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.util.SoundId;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


final class GiveSubcommand extends AbstractSubcommand
{
	GiveSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "give";
		this.permissionNode = "lodestar.give";
		this.usageString = "/lodestar give <player> [quantity] [destination_name]";
		this.description = MessageId.COMMAND_SUCCESS_HELP_GIVE;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
	                                  final String alias,
	                                  final String[] args)
	{
		switch (args.length)
		{
			case 2 ->
			{
				return null; // null return gives list of matching online player names
			}
			case 3 ->
			{
				// get list of all destination names
				List<String> destinationNames = new ArrayList<>(ctx.datastore().destinations().names());

				// add home and spawn destinations to list
				destinationNames.addFirst(ctx.messageBuilder().constants().getString(LodeStarUtility.SPAWN_KEY).orElse("Spawn"));
				destinationNames.addFirst(ctx.messageBuilder().constants().getString(LodeStarUtility.HOME_KEY).orElse("Home"));

				// return list filtered by matching prefix to argument
				return destinationNames.stream().filter(destinationName -> matchPrefix(destinationName, args[2])).collect(Collectors.toList());
			}
			default ->
			{
				return List.of();
			}
		}
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to give LodeStars, output error message and return
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_GIVE_PERMISSION_DENIED).send();
			return true;
		}

		// if too few arguments, send error and usage message and return
		if (args.size() < getMinArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		// get required argument target player name and remove from args list
		String targetPlayerName = args.removeFirst();

		// get player by name
		Player targetPlayer = ctx.plugin().getServer().getPlayer(targetPlayerName);

		// if null, send player not found message and return
		if (targetPlayer == null)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PLAYER_NOT_FOUND)
					.setMacro(Macro.PLAYER, targetPlayerName)
					.send();
			return true;
		}

		// if not online, send player not online message and return
		if (!targetPlayer.isOnline())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PLAYER_NOT_ONLINE)
					.setMacro(Macro.PLAYER, targetPlayerName)
					.send();
			return true;
		}

		// get quantity if present
		int quantity = 1;
		if (!args.isEmpty() && isInteger(args.getFirst()))
		{
			quantity = Integer.parseInt(args.removeFirst());
		}

		// if no remaining arguments, give item in hand
		if (args.isEmpty())
		{
			giveItemInHand(sender, quantity, targetPlayer);
		}
		// try to parse all remaining arguments as destinationName
		else
		{
			Destination destination = ctx.lodeStarUtility().getDestination(getEnteredName(args));
			if (destination instanceof ValidDestination validDestination)
			{
				ItemStack giftItem = ctx.lodeStarUtility().create(quantity, validDestination.displayName());
				switch (giveNewItem(sender, targetPlayer, quantity, validDestination))
				{
					case SUCCESS_GIVE_SELF -> sendSuccessGiveSelfMessage(sender, giftItem);
					case SUCCESS_GIVE_OTHER -> sendSuccessGiveOtherMessage(sender, targetPlayer, giftItem);
					case FAIL_INVALID_ITEM -> sendInvalidItemMessage(sender);
					case FAIL_INVALID_DESTINATION -> sendInvalidDestinationMessage(sender);
					case FAIL_INVENTORY_FULL -> sendInventoryFullMessage(sender);
				}
			}
			else
			{
				sendInvalidItemMessage(sender);
			}
		}
		return true;
	}


	private static @NotNull String getEnteredName(List<String> args)
	{
		// join remaining arguments with spaces
		String result = String.join(" ", args);

		// strip legacy color codes from entered name
		result = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', result));

		// perform any additional string sanitization here

		// return sanitized destination anme string
		return result;
	}


	private void giveItemInHand(CommandSender sender, int quantity, Player targetPlayer)
	{
		// if sender is not player, send args-count-under error message
		if (!(sender instanceof Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
		}
		else
		{
			// get clone of item in player main hand
			ItemStack giftItem = player.getInventory().getItemInMainHand().clone();

			// get destination from item
			Destination destination = ctx.lodeStarUtility().getDestination(giftItem);

			if (destination instanceof ValidDestination validDestination)
			{
				switch (giveNewItem(sender, targetPlayer, quantity, validDestination))
				{
					case SUCCESS_GIVE_SELF -> sendSuccessGiveSelfMessage(sender, giftItem);
					case SUCCESS_GIVE_OTHER -> sendSuccessGiveOtherMessage(sender, targetPlayer, giftItem);
					case FAIL_INVALID_ITEM -> sendInvalidItemMessage(sender);
					case FAIL_INVALID_DESTINATION -> sendInvalidDestinationMessage(sender);
					case FAIL_INVENTORY_FULL -> sendInventoryFullMessage(sender);
				}
			}
		}
	}


	/**
	 * Helper method for give command
	 *
	 * @param giver        the player issuing the command
	 * @param targetPlayer the player being given item
	 * @param quantity     the size of the new ItemStack
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private GiveResult giveNewItem(final CommandSender giver,
	                               final Player targetPlayer,
	                               final int quantity,
	                               final ValidDestination validDestination)
	{
		ItemStack itemStack = ctx.lodeStarUtility().create(quantity, validDestination.displayName());

		if (itemStack != null)
		{
			int giveQuantity = itemStack.getAmount();
			int maxGiveAmount = ctx.plugin().getConfig().getInt("max-give-amount");

			// check quantity against configured max give amount
			if (maxGiveAmount >= 0)
			{
				giveQuantity = Math.min(maxGiveAmount, giveQuantity);
				itemStack.setAmount(giveQuantity);
			}

			// add stack of LodeStars to target player inventory
			HashMap<Integer, ItemStack> noFit = targetPlayer.getInventory().addItem(itemStack);

			// count items that didn't fit in inventory
			int noFitCount = 0;
			for (int index : noFit.keySet())
			{
				noFitCount += noFit.get(index).getAmount();
			}

			// if remaining items equals quantity given, send player-inventory-full message and return
			if (noFitCount == quantity) return GiveResult.FAIL_INVENTORY_FULL;
			else if (giver.getName().equals(targetPlayer.getName())) return GiveResult.SUCCESS_GIVE_SELF;
			else return GiveResult.SUCCESS_GIVE_OTHER;
		}
		else
		{
			return GiveResult.FAIL_INVALID_ITEM;
		}
	}


	private void sendSuccessGiveOtherMessage(final CommandSender sender, final Player targetPlayer, final ItemStack itemStack)
	{
		if (ctx.lodeStarUtility().getDestination(itemStack) instanceof ValidDestination validDestination)
		{
			// send message to command sender
			ctx.messageBuilder().sounds().play(sender, SoundId.COMMAND_SUCCESS_GIVE_SENDER);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_GIVE_OTHER)
					.setMacro(Macro.PLAYER, targetPlayer)
					.setMacro(Macro.ITEM, itemStack)
					.setMacro(Macro.DESTINATION, validDestination)
					.send();

			// send message to gift recipient
			ctx.messageBuilder().compose(targetPlayer, MessageId.COMMAND_SUCCESS_GIVE_TARGET)
					.setMacro(Macro.PLAYER, sender)
					.setMacro(Macro.ITEM, itemStack)
					.setMacro(Macro.DESTINATION, validDestination)
					.send();
		}
		else
		{
			sendInvalidDestinationMessage(sender);
		}
	}


	private void sendSuccessGiveSelfMessage(final CommandSender sender, final ItemStack itemStack)
	{
		if (ctx.lodeStarUtility().getDestination(itemStack) instanceof ValidDestination validDestination)
		{
			ctx.messageBuilder().sounds().play(sender, SoundId.COMMAND_SUCCESS_GIVE_TARGET);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_GIVE_SELF)
					.setMacro(Macro.ITEM, itemStack)
					.setMacro(Macro.DESTINATION, validDestination)
					.send();
		}
		else
		{
			sendInvalidDestinationMessage(sender);
		}
	}


	private void sendInventoryFullMessage(CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_GIVE_INVENTORY_FULL).send();
	}


	private void sendInvalidDestinationMessage(CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION).send();
	}


	private void sendInvalidItemMessage(CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_ITEM).send();
	}


	private enum GiveResult
	{
		SUCCESS_GIVE_OTHER,
		SUCCESS_GIVE_SELF,
		FAIL_INVENTORY_FULL,
		FAIL_INVALID_ITEM,
		FAIL_INVALID_DESTINATION,
	}


	private boolean isInteger(final String string)
	{
		try
		{
			Integer.parseInt(string);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

}
