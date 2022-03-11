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
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;


final class DestroyCommand extends SubcommandAbstract {

	private final PluginMain plugin;


	DestroyCommand(final PluginMain plugin) {
		this.plugin = plugin;
		this.name = "destroy";
		this.permissionNode = "lodestar.destroy";
		this.usageString = "/lodestar destroy";
		this.description = MessageId.COMMAND_HELP_DESTROY;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check that sender has permission
		if (!sender.hasPermission(permissionNode)) {
			plugin.messageBuilder.compose(sender, MessageId.PERMISSION_DENIED_DESTROY).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// cast sender to player
		Player player = (Player) sender;

		// get item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// check that item player is holding is a LodeStar item
		if (!plugin.lodeStarFactory.isItem(playerItem)) {
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_ITEM).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// destroy item stack, getting number of items destroyed
		int quantity = destroyItemStack(player, playerItem);

		// send item destroyed message
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_SUCCESS_DESTROY)
				.setMacro(Macro.ITEM_QUANTITY, quantity)
				.setMacro(Macro.DESTINATION, plugin.lodeStarFactory.getDestinationName(playerItem))
				.send();
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_DESTROY);

		return true;
	}


	/**
	 * Set item stack quantity to zero
	 *
	 * @param player the player whose item stack is to be destroyed
	 * @param playerItem the itemstack to destroy
	 * @return the number of items destroyed
	 */
	private int destroyItemStack(final Player player, final ItemStack playerItem) {
		int quantity = playerItem.getAmount();
		playerItem.setAmount(0);
		player.getInventory().setItemInMainHand(playerItem);
		return quantity;
	}

}
