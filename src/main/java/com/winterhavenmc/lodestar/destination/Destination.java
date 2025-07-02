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

import java.util.Objects;
import java.util.UUID;


/**
 * Represents an immutable location and display name. Location is decomposed into validated, immutable final fields,
 * and reconstituted into a Bukkit location on access. Static factory methods are provided to create new instances
 * when reading from a datastore, or creating an instance manually.
 */
public sealed interface Destination permits ValidDestination, InvalidDestination
{
	enum Type { HOME, SPAWN, STORED }


	/**
	 * Returns an instance of a destination of the appropriate type, or invalid if a destination could not be created
	 *
	 * @param type        the type of destination
	 * @param displayName the display name of the destination
	 * @param location    the location of the destination
	 * @return a subclass of {@link ValidDestination}, or an {@link InvalidDestination} if no destination could be creaated
	 */
	static Destination of(final Type type,
	                      final String displayName,
	                      final Location location)
	{
		return Destination.of(type, displayName, Objects.requireNonNull(location.getWorld()).getName(),
				location.getWorld().getUID(), location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
	}


	/**
	 * Create an instance of a {@link Destination} from values retrieved from the datastore
	 * @return a subclass of {@link ValidDestination}, or an {@link InvalidDestination} if no destination could be creaated
	 */
	static Destination of(final Type type,
	                      final String displayName,
	                      final String worldName,
	                      final UUID worldUid,
	                      final double x,
	                      final double y,
	                      final double z,
	                      final float yaw,
	                      final float pitch)
	{
		if (displayName == null) return new InvalidDestination("�", "The display name was null.");
		else if (displayName.isBlank()) return new InvalidDestination("⬚", "The display name was blank.");
		else if (worldName == null) return new InvalidDestination(displayName, "The world name was null.");
		else if (worldName.isBlank()) return new InvalidDestination(displayName, "The world name was blank.");
		else if (worldUid == null) return new InvalidDestination(displayName, "The world UUID was null.");
		else if (type == null) return new InvalidDestination(displayName, "The type was null.");

		return switch (type)
		{
			case HOME -> new HomeDestination(displayName, worldName, worldUid, x, y, z, yaw, pitch);
			case SPAWN -> new SpawnDestination(displayName,worldName, worldUid, x, y, z, yaw, pitch);
			case STORED -> new StoredDestination(displayName, worldName, worldUid, x, y, z, yaw, pitch);
		};
	}

}
