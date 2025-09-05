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

package com.winterhavenmc.lodestar.plugin.util;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


/**
 * Factory class for creating and testing SpawnStar item stacks
 */
public final class LodeStarUtility
{
	public static final String ITEM_KEY = "LODESTAR";
	public static final Material DEFAULT_MATERIAL = Material.NETHER_STAR;
	private final MessageBuilder messageBuilder;


	public LodeStarUtility(final MessageBuilder messageBuilder)
	{
		this.messageBuilder = messageBuilder;
	}


	/**
	 * Create a SpawnStar item stack of given quantity, with custom display name and lore
	 *
	 * @param passedQuantity number of SpawnStar items in newly created stack
	 * @return ItemStack of SpawnStar items
	 */
	public ItemStack create(final int passedQuantity)
	{
		int quantity = passedQuantity;
		quantity = Math.max(1, quantity);

		Optional<ItemStack> itemStack = messageBuilder.itemForge().createItem(ITEM_KEY);
		if (itemStack.isPresent())
		{
			ItemStack returnItem = itemStack.get();
			quantity = Math.min(quantity, returnItem.getMaxStackSize());
			returnItem.setAmount(quantity);
			return returnItem;
		}
		else
		{
			return null;
		}
	}
}
