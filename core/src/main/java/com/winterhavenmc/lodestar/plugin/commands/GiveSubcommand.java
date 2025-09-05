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

import com.winterhavenmc.library.messagebuilder.ItemForge;
import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.InvalidDestination;
import com.winterhavenmc.lodestar.models.destination.ValidDestination;
import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.LodeStarUtility;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;


final class GiveSubcommand extends AbstractSubcommand
{
	GiveSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "give";
		this.permissionNode = "lodestar.give";
		this.usageString = "/lodestar give <player> [quantity] [destination_name]";
		this.description = MessageId.COMMAND_HELP_GIVE;
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
				// get all destination keys in list
				List<String> destinationNames = new ArrayList<>(ctx.datastore().destinations().names());

				// add home and spawn destinations to list
				destinationNames.addFirst(ctx.messageBuilder().getConstantResolver().getString("LOCATION.SPAWN").orElse("Spawn"));
				destinationNames.addFirst(ctx.messageBuilder().getConstantResolver().getString("LOCATION_HOME").orElse("Home"));

				// return list filtered by matching prefix to argument
				return destinationNames.stream().filter(destinationKey -> matchPrefix(destinationKey, args[2])).collect(Collectors.toList());
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
		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_GIVE).send();
			return true;
		}

		// if too few arguments, send error and usage message
		if (args.size() < getMinArgs())
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			return true;
		}

		// get required argument target player name and remove from ArrayList
		String targetPlayerName = args.removeFirst();

		// get player by name
		Player targetPlayer = ctx.plugin().getServer().getPlayer(targetPlayerName);

		// if no match, send player not found message and return
		if (targetPlayer == null)
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PLAYER_NOT_FOUND)
					.setMacro(Macro.PLAYER, targetPlayerName)
					.send();
			return true;
		}


		// target player is valid; create object to give:
		// if first arg is integer, use as quantity and remove arg
		// if remaining args exist, try to match destination name
		// else no remaining args, check item in hand is lodestar and create clone

		// get quantity if present
		int quantity = 1;
		if (!args.isEmpty() && isInteger(args.getFirst()))
		{
			quantity = Integer.parseInt(args.removeFirst());
		}

		// if no remaining arguments, check if item in hand is LodeStar item
		if (args.isEmpty())
		{
			// if sender is not player, send args-count-under error message
			if (!(sender instanceof Player player))
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
				displayUsage(sender);
				return true;
			}

			// get clone of item in player main hand
			ItemStack giftItem = player.getInventory().getItemInMainHand().clone();
			giftItem.setAmount(quantity);

			if (ItemForge.isCustomItem(giftItem))
			{
				switch (giveItem(sender, targetPlayer, giftItem))
				{
					case SUCCESS_GIVE_SELF -> { }
					case SUCCESS_GIVE_OTHER -> sendSuccessGiveOtherMessage(sender, targetPlayer, giftItem);
					case FAIL_INVALID_ITEM -> sendInvalidItemMessage(sender);
					case FAIL_INVALID_DESTINATION -> sendInvalidDestinationMessage(sender);
					case FAIL_INVENTORY_FULL -> sendInventoryFullMessage(sender);
				}
			}
			return true;
		}

		// try to parse all remaining arguments as destinationName
		else
		{
			// join remaining arguments with spaces
			String destinationName = String.join(" ", args);

			//TODO: destination is spawn or home, set appropriate destination, else attempt to retrieve stored destination

			switch (destinationName)
			{

			}
			if (destinationName.equalsIgnoreCase(ctx.messageBuilder().getConstantResolver().getString("LOCATIONS.SPAWN").orElse("Spawn"))
				|| destinationName.equalsIgnoreCase("spawn")
			{

			}



			// attempt to get destination by name from datastre
			Destination destination = ctx.datastore().destinations().get(destinationName);

			// if resulting name is existing destination, get destinationName from datastore
			if (destination instanceof ValidDestination validDestination)
			{
				destinationName = validDestination.displayName();

				// give item to sender and return
				ctx.messageBuilder().itemForge().createItem(destinationName).ifPresent(itemStack -> giveItem(sender, targetPlayer, itemStack));
			}
			return true;
		}
	}


	private Material getMaterial(String argMaterialName)
	{
		Material material = LodeStarUtility.DEFAULT_MATERIAL;
		String defaultMaterialName = ctx.plugin().getConfig().getString("default-material");

		if (ctx.plugin().getConfig().getBoolean("default-material-only")
			&& defaultMaterialName != null
			&& Material.matchMaterial(defaultMaterialName) != null)
		{
			material = Material.matchMaterial(Objects.requireNonNull(ctx.plugin().getConfig().getString("default-material")));
		}
		else if (Material.matchMaterial(argMaterialName) != null)
		{
			material = Material.matchMaterial(argMaterialName);
		}
		return material;
	}


	/**
	 * Helper method for give command
	 *
	 * @param giver        the player issuing the command
	 * @param targetPlayer the player being given item
	 * @param itemStack    the LodeStar item being given
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	@SuppressWarnings("UnusedReturnValue")
	private GiveResult giveItem(final CommandSender giver, final Player targetPlayer, final ItemStack itemStack)
	{
		if (!ItemForge.isCustomItem(itemStack)) return GiveResult.FAIL_INVALID_ITEM;
		else if (!(getDestination(itemStack) instanceof ValidDestination)) return GiveResult.FAIL_INVALID_DESTINATION;
		else
		{
			int quantity = itemStack.getAmount();
			int maxGiveAmount = ctx.plugin().getConfig().getInt("max-give-amount");

			// check quantity against configured max give amount
			if (maxGiveAmount >= 0)
			{
				quantity = Math.min(maxGiveAmount, quantity);
				itemStack.setAmount(quantity);
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
	}


	private void sendSuccessGiveOtherMessage(final CommandSender sender, final Player targetPlayer, final ItemStack itemStack)
	{
		if (getDestination(itemStack) instanceof ValidDestination validDestination)
		{
			// send message to command sender
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_GIVE_SENDER);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_GIVE)
					.setMacro(Macro.DESTINATION, validDestination)
					.setMacro(Macro.PLAYER, targetPlayer)
					.send();

			// send message to gift recipient
			ctx.soundConfig().playSound(targetPlayer, SoundId.COMMAND_SUCCESS_GIVE_TARGET);
			ctx.messageBuilder().compose(targetPlayer, MessageId.COMMAND_SUCCESS_GIVE_TARGET)
					.setMacro(Macro.DESTINATION, validDestination)
					.setMacro(Macro.PLAYER, sender)
					.send();
		}
		else
		{
			sendInvalidDestinationMessage(sender);
		}
	}


	private void sendInventoryFullMessage(CommandSender sender)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_GIVE_INVENTORY_FULL).send();
	}


	private void sendInvalidDestinationMessage(CommandSender sender)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION).send();
	}

	private void sendInvalidItemMessage(CommandSender sender)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
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


	private ItemStack setDestination(final ItemStack itemStack, final ValidDestination validDestination)
	{
		NamespacedKey namespacedKey = new NamespacedKey(ctx.plugin(), "DESTINATION");
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null)
		{
			itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, validDestination.key());
			itemStack.setItemMeta(itemMeta);
		}
		return itemStack;
	}


	private Destination getDestination(final ItemStack itemStack)
	{
		NamespacedKey namespacedKey = new NamespacedKey(ctx.plugin(), "DESTINATION");
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null)
		{
			String destinationName = itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
			return ctx.datastore().destinations().get(destinationName);
		}
		else
		{
			return new InvalidDestination("UNKNOWN", "Invalid destination");
		}
	}

}
