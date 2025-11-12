/*
 * Copyright (c) 2025 Tim Savage.
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

package com.winterhavenmc.lodestar.plugin.ports.teleporter;

import com.winterhavenmc.lodestar.plugin.LodeStarPluginController;
import org.bukkit.entity.Player;

public interface TeleportHandler
{
	TeleportHandler init(LodeStarPluginController.TeleporterContextContainer ctx);

	/**
	 * Start the player teleport
	 *
	 * @param player the player being teleported
	 */
	void initiateTeleport(Player player);

	/**
	 * Cancel pending teleport for player
	 *
	 * @param player the player to cancel teleport
	 */
	void cancelTeleport(Player player);

	/**
	 * Test if player uuid is in warmup map
	 *
	 * @param player the player to test if in warmup map
	 * @return {@code true} if player is in warmup map, {@code false} if not
	 */
	boolean isWarmingUp(Player player);

	/**
	 * Remove player uuid from warmup map
	 *
	 * @param player the player to remove from the warmup map
	 */
	void removeWarmingUpPlayer(Player player);

	/**
	 * Insert player into cooldown map
	 *
	 * @param player the player being inserted into the cooldown map
	 */
	void startPlayerCooldown(Player player);

	/**
	 * Remove player from cooldown map
	 *
	 * @param player the player to be removed from the cooldown map
	 */
	void cancelPlayerCooldown(Player player);

	/**
	 * Test if a player is currently in the cooldown map
	 *
	 * @param player the player to check
	 * @return true if player is currently in the cooldown map, false if not
	 */
	boolean isCoolingDown(Player player);
}
