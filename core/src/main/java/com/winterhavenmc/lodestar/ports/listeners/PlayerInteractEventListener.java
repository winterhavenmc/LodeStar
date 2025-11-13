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

package com.winterhavenmc.lodestar.ports.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public interface PlayerInteractEventListener extends Listener
{
	/**
	 * Event listener for PlayerInteractEvent<br>
	 * detects LodeStar use, or cancels teleport
	 * if cancel-on-interaction configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event);

	/**
	 * Check if cancel on interaction is configured and if such interaction has occurred
	 *
	 * @param event the event to check for player/block interaction
	 * @return true if cancellable interaction occurred, false if not
	 */
	boolean cancelTeleportOnInteraction(PlayerInteractEvent event);
}
