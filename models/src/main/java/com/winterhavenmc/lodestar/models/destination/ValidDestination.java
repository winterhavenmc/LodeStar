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

package com.winterhavenmc.lodestar.models.destination;

import com.winterhavenmc.library.messagebuilder.pipeline.adapters.displayname.DisplayNameable;

import static org.bukkit.ChatColor.stripColor;
import static org.bukkit.ChatColor.translateAlternateColorCodes;


/**
 * Record class that Represents a valid destination with accessor methods for destination fields and derived values
 */
public sealed interface ValidDestination extends Destination, DisplayNameable permits HomeDestination, SpawnDestination, StoredDestination, TeleportDestination
{
	String displayName();


	/**
	 * Accessor for derived value for destination key field
	 *
	 * @return the value of the key field
	 */
	default String key()
	{
		return stripColor(translateAlternateColorCodes('&', this.displayName()))
				.replace(' ', '_');
	}


	/**
	 * Accessor method to fulfill DisplayNameable interface
	 *
	 * @return the display name of the destination
	 */
	default String getDisplayName()
	{
		return displayName();
	}

}
