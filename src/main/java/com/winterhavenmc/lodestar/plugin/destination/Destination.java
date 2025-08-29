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

package com.winterhavenmc.lodestar.plugin.destination;

import com.winterhavenmc.lodestar.plugin.destination.location.*;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;


/**
 * Represents an immutable location and a corresponding display name. A {@link Location} is decomposed into validated,
 * immutable fields, and reconstituted into a Bukkit {@code Location} on access. Static factory methods are provided to
 * create new instances when reading from a datastore, or creating an instance from an existing Bukkit {@code Location}
 * and a corresponding {@code String} display name.
 */
public sealed interface Destination permits ValidDestination, InvalidDestination
{
	/**
	 * Enumerates the types of valid destinations.
	 */
	enum Type { HOME, SPAWN, STORED }


	/**
	 * Returns a new instance of a {@code Destination} of the appropriate type from the given parameters.
	 *
	 * @param type        the type of destination
	 * @param displayName the display name of the destination
	 * @param location    the location of the destination
	 * @return a {@link ValidDestination}, or an {@link InvalidDestination} if a valid destination could not be creaated.
	 */
	static Destination of(final @NotNull Type type,
	                      final @NotNull String displayName,
	                      final @NotNull Location location)
	{
		return Destination.of(type, displayName, Objects.requireNonNull(location.getWorld()).getName(),
				location.getWorld().getUID(), location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
	}


	/**
	 * Create a new instance of a {@code Destination} from values retrieved from the datastore.
	 *
	 * @return a {@link ValidDestination}, or an {@link InvalidDestination} if a valid destination could not be creaated.
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
		if (displayName == null) return new InvalidDestination("∅", "The display name was null.");
		if (displayName.isBlank()) return new InvalidDestination("⬚", "The display name was blank.");
		if (type == null) return new InvalidDestination(displayName, "The type was null.");

		return switch (ImmutableLocation.of(worldName, worldUid, x, y, z, yaw, pitch))
		{
			case InvalidLocation ignored -> new InvalidDestination(displayName, "The location was invalid.");
			case NoWorldLocation ignored -> new InvalidDestination(displayName, "The location had an invalid world.");
			case UnloadedWorldLocation ignored -> new InvalidDestination(displayName, "The location world is unloaded.");
			case ValidLocation validLocation -> switch (type)
					{
						case HOME -> new HomeDestination(displayName, validLocation);
						case SPAWN -> new SpawnDestination(displayName, validLocation);
						case STORED -> new StoredDestination(displayName, validLocation);
					};
		};
	}

}
