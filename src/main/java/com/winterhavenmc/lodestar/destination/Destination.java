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

package com.winterhavenmc.lodestar.destination;

import org.bukkit.Location;

import java.util.UUID;


public sealed interface Destination permits ValidDestination, InvalidDestination
{
	enum Type { STORED, HOME, SPAWN }


	/**
	 * Returns an instance of a destination of the appropriate type, or invalid if a destination could not be created
	 *
	 * @param displayName the display name of the destination
	 * @param location the location of the destination
	 * @param type the type of destination
	 * @return a subclass of {@link ValidDestination}, or an {@link InvalidDestination} if no destination could be creaated
	 */
	static Destination of(final String displayName,
	                      final Location location,
	                      final Type type)
	{
		if (displayName == null) return new InvalidDestination("NULL", "Destination display name was null.");
		else if (displayName.isBlank()) return new InvalidDestination("BLANK", "Destination display name was blank.");
		else if (location == null) return new InvalidDestination(displayName, "Destination location was null.");
		else if (type == null) return new InvalidDestination(displayName, "Destination type was null.");
		else if (type == Type.HOME) return new HomeDestination(displayName, location);
		else if (type == Type.SPAWN) return new SpawnDestination(displayName, location);
		else return new StoredDestination(displayName, location);
	}


	/**
	 * Create an instance of a {@link Destination} from values retrieved from the datastore
	 * @return a subclass of {@link ValidDestination}, or an {@link InvalidDestination} if no destination could be creaated
	 */
	static Destination of(final Type type,
						  final String displayName,
	                      final boolean worldValid,
	                      final String worldName,
	                      final UUID worldUid,
	                      final double x,
	                      final double y,
	                      final double z,
	                      final float yaw,
	                      final float pitch)
	{
		if (displayName == null) return new InvalidDestination("NULL", "The display name was null.");
		else if (displayName.isBlank()) return new InvalidDestination("BLANK", "The display name was blank.");
		else if (type == null) return new InvalidDestination(displayName, "The destination type was null.");
		else if (worldName == null) return new InvalidDestination(displayName, "The world name was null.");
		else if (worldName.isBlank()) return new InvalidDestination(displayName, "The world name was blank.");
		else if (worldUid == null) return new InvalidDestination(displayName, "The world UUID was null.");
		else if (type == Type.HOME) return new HomeDestination(displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);
		else if (type == Type.SPAWN) return new SpawnDestination(displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);
		else return new StoredDestination(displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);
	}

}
