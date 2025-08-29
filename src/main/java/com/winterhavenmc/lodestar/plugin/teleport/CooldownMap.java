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

package com.winterhavenmc.lodestar.plugin.teleport;

import com.winterhavenmc.lodestar.plugin.PluginMain;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.winterhavenmc.library.TimeUnit.SECONDS;


class CooldownMap
{
	private final PluginMain plugin;

	// hashmap to store player UUID and cooldown expire time in milliseconds
	private final ConcurrentHashMap<UUID, Long> cooldownMap;


	CooldownMap(final PluginMain plugin)
	{
		this.plugin = plugin;
		cooldownMap = new ConcurrentHashMap<>();
	}


	/**
	 * Insert player uuid into cooldown hashmap with {@code expiretime} as value.<br>
	 * Schedule task to remove player uuid from cooldown hashmap when time expires.
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void startPlayerCooldown(final Player player)
	{
		int cooldownSeconds = plugin.getConfig().getInt("teleport-cooldown");

		Long expireTime = System.currentTimeMillis() + (SECONDS.toMillis(cooldownSeconds));
		cooldownMap.put(player.getUniqueId(), expireTime);

		new RemovePlayerCooldownTask(plugin, player).runTaskLater(plugin, SECONDS.toTicks(cooldownSeconds));
	}


	/**
	 * Get time remaining for player cooldown
	 *
	 * @param player the player whose cooldown time remaining is being retrieved
	 * @return long remaining time in milliseconds
	 */
	long getCooldownTimeRemaining(final Player player)
	{
		long remainingTime = 0;
		if (cooldownMap.containsKey(player.getUniqueId()))
		{
			remainingTime = (cooldownMap.get(player.getUniqueId()) - System.currentTimeMillis());
		}
		return remainingTime;
	}


	/**
	 * Test if player is currently cooling down after item use
	 *
	 * @param player the player to check for cooldown
	 * @return boolean - {@code true} if player is cooling down after item use, {@code false} if not
	 */
	boolean isCoolingDown(final Player player)
	{
		return getCooldownTimeRemaining(player) > 0;
	}


	void removePlayer(final Player player)
	{
		cooldownMap.remove(player.getUniqueId());
	}

}
