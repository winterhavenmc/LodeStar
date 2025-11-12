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

package com.winterhavenmc.lodestar.adapters.teleporter.bukkit;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;


class WarmupMap
{
	// HashMap containing player UUID as key and warmup task id as value
	private final HashMap<UUID, Integer> warmupMap;


	WarmupMap()
	{
		// initialize warmup HashMap
		warmupMap = new HashMap<>();
	}


	/**
	 * Remove player uuid from warmup hashmap.
	 *
	 * @param player the player to remove from the warmup map
	 */
	void removePlayer(final Player player)
	{
		warmupMap.remove(player.getUniqueId());
	}


	/**
	 * Test if player uuid is in warmup hashmap
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	boolean isWarmingUp(final Player player)
	{
		return warmupMap.containsKey(player.getUniqueId());
	}


	/**
	 * Insert player uuid and taskId into warmup hashmap.
	 *
	 * @param player the player to be inserted in the warmup map
	 * @param taskId the taskId of the player's delayed teleport task
	 */
	void startPlayerWarmUp(final Player player, final Integer taskId)
	{
		warmupMap.put(player.getUniqueId(), taskId);
	}


	boolean containsPlayer(final Player player)
	{
		return warmupMap.containsKey(player.getUniqueId());
	}


	int getTaskId(final Player player)
	{
		return warmupMap.get(player.getUniqueId());
	}

}
