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

package com.winterhavenmc.lodestar.models.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;


/**
 * Represents a validated, type-safe {@link Location}. Returns an appropriate subtype based on the results of validation.
 * <p>
 * <strong>Note:</strong> The server world referenced by the uuid stored in this object may still become invalid
 * after object creation. There can be no guarantee that a world referenced by this object remains available.
 * However, objects that implement this interface are immutable, and may still be used to create or update records
 * in the datastore, or used to display messages.
 */
public sealed interface ImmutableLocation permits ValidLocation, InvalidLocation, NoWorldLocation, UnloadedWorldLocation
{
	/**
	 * Static factory method to create an {@code ImmutableLocaltion}
	 *
	 * @param location a Bukkit {@link Location}
	 * @return an instance of an {@code ImmutableLocation} of the appropriate subtype
	 */
	static ImmutableLocation of(final Location location)
	{
		if (location == null) return new InvalidLocation("The location was null.");
		else if (location.getWorld() == null) return new NoWorldLocation("∅", new UUID(0, 0),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		else if (!location.isWorldLoaded()) return new UnloadedWorldLocation(location.getWorld().getName(), location.getWorld().getUID(),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		else return new ValidLocation(location.getWorld().getName(), location.getWorld().getUID(),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}


	/**
	 * Static factory method to create an {@code ImmutableLocaltion}
	 *
	 * @return an instance of an {@code ImmutableLocation} of the appropriate subtype
	 */
	static ImmutableLocation of(String worldName, UUID worldUid, double x, double y, double z, float yaw, float pitch)
	{
		String checkedName = (worldName != null) ? worldName : "∅";
		checkedName = (!checkedName.isBlank()) ? checkedName : "⬚";

		return (Bukkit.getWorld(worldUid) == null)
				? new NoWorldLocation(checkedName, new UUID(0, 0), x, y, z, yaw, pitch)
				: new ValidLocation(worldName, worldUid, x, y, z, yaw, pitch);
	}

}
