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

import com.winterhavenmc.lodestar.plugin.PluginController;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;


final class GiveSubcommand extends AbstractSubcommand
{
	GiveSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "give";
		this.permissionNode = "lodestar.give";
		this.usageString = "/lodestar give <player> [quantity] [material] [destination_name]";
		this.description = MessageId.COMMAND_HELP_GIVE;
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
	                                  final String alias,
	                                  final String[] args)
	{
		if (args.length == 2)
		{
			// return all matching online players, including invisible players
			return ctx.plugin().getServer().getOnlinePlayers().stream()
					.map(Player::getName)
					.filter(playerName -> matchPrefix(playerName, args[1]))
					.collect(Collectors.toList());
		}

		else if (args.length == 3)
		{
			// get all destination keys in list
			List<String> destinationNames = new ArrayList<>(ctx.datastore().destinations().names());

			// add home and spawn destinations to list
			destinationNames.addFirst(ctx.messageBuilder().getConstantResolver().getString("LOCATION.SPAWN").orElse("Spawn"));
			destinationNames.addFirst(ctx.messageBuilder().getConstantResolver().getString("LOCATION_HOME").orElse("Home"));

			// return list filtered by matching prefix to argument
			return destinationNames.stream().filter(destinationKey -> matchPrefix(destinationKey, args[2])).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_GIVE).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if too few arguments, send error and usage message
		if (args.size() < getMinArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
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
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PLAYER_NOT_FOUND).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}


		// initialize destinationName to empty string
		String destinationName = "";

		// initialize default quantity
		int quantity = 1;

		// initialize material as null
		Material material = null;

		// try to parse first argument as integer quantity
		if (!args.isEmpty())
		{
			try
			{
				quantity = Integer.parseInt(args.getFirst());

				// remove argument if no exception thrown
				args.removeFirst();
			} catch (NumberFormatException e)
			{
				// not an integer, do nothing
			}
		}

		// if no remaining arguments, check if item in hand is LodeStar item
		if (args.isEmpty())
		{
			// if sender is not player, send args-count-under error message
			if (!(sender instanceof Player))
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
				displayUsage(sender);
				return true;
			}

			// get clone of item in player hand
			ItemStack playerItem = ((Player) sender).getInventory().getItemInMainHand().clone();

			// if item in hand is a LodeStar item, set destination and material from item
			if (ctx.lodeStarUtility().isItem(playerItem))
			{
				destinationName = ctx.lodeStarUtility().getDisplayName(playerItem).orElse(null);
				material = playerItem.getType();
			}
		}

		// try to parse all remaining arguments as destinationName
		else
		{
			// join remaining arguments with spaces
			String testName = String.join(" ", args);

			// if resulting name is existing destination, get destinationName from datastore
			if (ctx.lodeStarUtility().destinationExists(testName))
			{
				destinationName = ctx.lodeStarUtility().getDisplayName(testName).orElse(null);

				// remove remaining arguments
				args.clear();
			}
		}

		// try to parse next argument as material
		if (!args.isEmpty())
		{
			// try to match material
			material = Material.matchMaterial(args.getFirst());

			// if material matched, remove argument from list
			if (material != null)
			{
				args.removeFirst();
			}
		}

		// try to parse all remaining arguments as destinationName
		if (!args.isEmpty())
		{
			String testName = String.join(" ", args);

			// if resulting name is existing destination, get destinationName from datastore
			if (ctx.lodeStarUtility().destinationExists(testName))
			{
				destinationName = ctx.lodeStarUtility().getDisplayName(testName).orElse(null);

				// remove remaining arguments
				args.clear();
			}

			// else given destination is invalid (but not blank), so send error message
			else
			{
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_DESTINATION)
						.setMacro(Macro.DESTINATION, testName)
						.send();
				ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// if no destination name set, set destination to spawn
		if (destinationName == null || destinationName.isEmpty())
		{
			destinationName = "spawn";
		}

		// if no material set or default-material-only configured true, try to parse material from config
		if (material == null || ctx.plugin().getConfig().getBoolean("default-material-only"))
		{
			material = Material.matchMaterial(Objects.requireNonNull(ctx.plugin().getConfig().getString("default-material")));
		}

		// if still no material match, set to nether star
		if (material == null)
		{
			material = Material.NETHER_STAR;
		}

		// create item stack with material, quantity and data
		ItemStack itemStack = new ItemStack(material, quantity);

		// set item metadata on item stack
		ctx.lodeStarUtility().setMetaData(itemStack, destinationName);

		// give item stack to target player
		giveItem(sender, targetPlayer, itemStack);

		return true;
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
	private boolean giveItem(final CommandSender giver, final Player targetPlayer, final ItemStack itemStack)
	{
		String key = ctx.lodeStarUtility().getKey(itemStack);
		int quantity = itemStack.getAmount();
		int maxGiveAmount = ctx.plugin().getConfig().getInt("max-give-amount");

		// check quantity against configured max give amount
		if (maxGiveAmount >= 0)
		{
			quantity = Math.min(maxGiveAmount, quantity);
			itemStack.setAmount(quantity);
		}

		// test that item is a LodeStar item
		if (!ctx.lodeStarUtility().isItem(itemStack))
		{
			ctx.messageBuilder().compose(giver, MessageId.COMMAND_FAIL_INVALID_ITEM).send();
			ctx.soundConfig().playSound(giver, SoundId.COMMAND_FAIL);
			return true;
		}

		// add specified quantity of LodeStars to player inventory
		HashMap<Integer, ItemStack> noFit = targetPlayer.getInventory().addItem(itemStack);

		// count items that didn't fit in inventory
		int noFitCount = 0;
		for (int index : noFit.keySet())
		{
			noFitCount += noFit.get(index).getAmount();
		}

		// if remaining items equals quantity given, send player-inventory-full message and return
		if (noFitCount == quantity)
		{
			ctx.messageBuilder().compose(giver, MessageId.COMMAND_FAIL_GIVE_INVENTORY_FULL)
					.setMacro(Macro.ITEM_QUANTITY, quantity)
					.send();
			return false;
		}

		// subtract noFitCount from quantity
		quantity = quantity - noFitCount;

		// get destination display name
		String destinationName = ctx.lodeStarUtility().getDisplayName(key).orElse(null);

		// don't display messages if giving item to self
		if (!giver.getName().equals(targetPlayer.getName()))
		{
			// send message and play sound to giver
			ctx.messageBuilder().compose(giver, MessageId.COMMAND_SUCCESS_GIVE)
					.setMacro(Macro.DESTINATION, destinationName)
					.setMacro(Macro.ITEM_QUANTITY, quantity)
					.setMacro(Macro.TARGET_PLAYER, targetPlayer)
					.send();

			// if giver is in game, play sound
			if (giver instanceof Player)
			{
				ctx.soundConfig().playSound(giver, SoundId.COMMAND_SUCCESS_GIVE_SENDER);
			}

			// send message to target player
			ctx.messageBuilder().compose(targetPlayer, MessageId.COMMAND_SUCCESS_GIVE_TARGET)
					.setMacro(Macro.DESTINATION, destinationName)
					.setMacro(Macro.ITEM_QUANTITY, quantity)
					.setMacro(Macro.TARGET_PLAYER, giver)
					.send();
		}

		// play sound to target player
		ctx.soundConfig().playSound(targetPlayer, SoundId.COMMAND_SUCCESS_GIVE_TARGET);
		return true;
	}

}
