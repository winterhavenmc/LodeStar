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

package com.winterhavenmc.lodestar.plugin.teleport;

import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import com.winterhavenmc.lodestar.plugin.tasks.RemovePlayerCooldownTask;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static com.winterhavenmc.library.time.TimeUnit.SECONDS;


class CooldownMap
{
	// hashmap to store player UUID and cooldown expire instant
	private final HashMap<UUID, Instant> cooldownMap;
	private final TeleportHandler teleportHandler;
	private final LodeStarPluginController.ContextContainer ctx;


	CooldownMap(final TeleportHandler teleportHandler, LodeStarPluginController.ContextContainer ctx)
	{
		this.teleportHandler = teleportHandler;
		this.ctx = ctx;
		cooldownMap = new HashMap<>();
	}


	/**
	 * Insert player uuid into cooldown hashmap with {@code expiretime} as value.<br>
	 * Schedule task to remove player uuid from cooldown hashmap when time expires.
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void startPlayerCooldown(final Player player)
	{
		int cooldownSeconds = ctx.plugin().getConfig().getInt("teleport-cooldown");
		cooldownMap.put(player.getUniqueId(), Instant.now().plusSeconds(cooldownSeconds));
		new RemovePlayerCooldownTask(player, teleportHandler).runTaskLater(ctx.plugin(), SECONDS.toTicks(cooldownSeconds));
	}


	/**
	 * Get time remaining for player cooldown
	 *
	 * @param player the player whose cooldown time remaining is being retrieved
	 * @return long remaining time in milliseconds
	 */
	Duration getCooldownTimeRemaining(final Player player)
	{
		if (cooldownMap.containsKey(player.getUniqueId()))
		{
			Duration remainingDuration = Duration.between(Instant.now(), cooldownMap.get(player.getUniqueId()));
			if (remainingDuration.isPositive())
			{
				return remainingDuration;
			}
		}
		return Duration.ZERO;
	}


	/**
	 * Test if player is currently cooling down after item use
	 *
	 * @param player the player to check for cooldown
	 * @return boolean - {@code true} if player is cooling down after item use, {@code false} if not
	 */
	boolean isCoolingDown(final Player player)
	{
		if (cooldownMap.containsKey(player.getUniqueId()))
		{
			return cooldownMap.get(player.getUniqueId()).isBefore(Instant.now());
		}
		return false;
	}


	/**
	 * Remove a player from the cooldown map
	 *
	 * @param player the player to be removed from the cooldown map
	 */
	void removePlayer(final Player player)
	{
		cooldownMap.remove(player.getUniqueId());
	}

}
