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
import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.util.Macro;
import com.winterhavenmc.lodestar.plugin.util.MessageId;
import com.winterhavenmc.lodestar.plugin.sounds.SoundId;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;


final class DestroySubcommand extends AbstractSubcommand
{
	DestroySubcommand(final LodeStarPluginController.CommandContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "destroy";
		this.permissionNode = "lodestar.destroy";
		this.usageString = "/lodestar destroy";
		this.description = MessageId.COMMAND_SUCCESS_HELP_DESTROY;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// sender must be in game player
		if (!(sender instanceof Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check that sender has permission
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_DESTROY_PERMISSION_DENIED).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// check that item player is holding is a LodeStar item
		if (ItemForge.isItem("LODESTAR", playerItem))
		{
			Destination destination = ctx.lodeStarUtility().getDestination(playerItem);
			ItemStack destroyedItemStack = destroyItemStack(player, playerItem);

			ctx.soundConfig().playSound(player, SoundId.COMMAND_SUCCESS_DESTROY);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_DESTROY)
					.setMacro(Macro.ITEM, destroyedItemStack)
					.setMacro(Macro.DESTINATION, destination)
					.send();
		}
		else
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_INVALID_ITEM)
					.setMacro(Macro.ITEM, playerItem)
					.send();
		}
		return true;
	}


	/**
	 * Set item stack quantity to zero
	 *
	 * @param player     the player whose item stack is to be destroyed
	 * @param playerItem the itemstack to destroy
	 * @return the number of items destroyed
	 */
	private ItemStack destroyItemStack(final Player player, final ItemStack playerItem)
	{
		ItemStack destroyedItems = playerItem.clone();
		playerItem.setAmount(0);
		player.getInventory().setItemInMainHand(playerItem);
		return destroyedItems;
	}

}
