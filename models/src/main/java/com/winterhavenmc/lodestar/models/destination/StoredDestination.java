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

import com.winterhavenmc.library.messagebuilder.pipeline.adapters.location.Locatable;
import com.winterhavenmc.lodestar.models.location.*;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


/**
 * Record class that represents a user-created destination, which can be persisted in the datastore
 */
public record StoredDestination(String displayName, ValidLocation location) implements ValidDestination, Locatable
{
	public static Destination of(final String displayName, final ValidLocation validLocation)
	{
		if (displayName == null) return new InvalidDestination("NULL", "The display name was null.");
		else if (displayName.isBlank()) return new InvalidDestination("BLANK", "The display name was blank.");
		else return new StoredDestination(displayName, validLocation);
	}


	/**
	 * Create a new instance of a {@code Destination} from values retrieved from the datastore.
	 *
	 * @return a {@link ValidDestination}, or an {@link InvalidDestination} if a valid destination could not be creaated.
	 */
	public static Destination of(final String displayName,
	                             final String worldName,
	                             final UUID worldUid,
	                             final double x,
	                             final double y,
	                             final double z,
	                             final float yaw,
	                             final float pitch)
	{
		if (displayName == null) return new InvalidDestination("∅", "The display name was null.");
		else if (displayName.isBlank()) return new InvalidDestination("⬚", "The display name was blank.");
		else return switch (ImmutableLocation.of(worldName, worldUid, x, y, z, yaw, pitch))
		{
			case InvalidLocation ignored -> new InvalidDestination(displayName, "The location was invalid.");
			case NoWorldLocation ignored -> new InvalidDestination(displayName, "The location had an invalid world.");
			case UnloadedWorldLocation ignored -> new InvalidDestination(displayName, "The location world is unloaded.");
			case ValidLocation validLocation -> new StoredDestination(displayName, validLocation);
		};
	}


	@Override
	public @NotNull String toString()
	{
		return(displayName + " | " + location.worldName() + " [" + location.x() + "," + location.y() + "." + location.z() + "]");
	}


	@Override
	public Location getLocation()
	{
		return this.location.getLocation();
	}

}
