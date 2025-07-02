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

package com.winterhavenmc.lodestar.destination.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public sealed interface ImmutableLocation permits InvalidLocation, NoWorldLocation, UnloadedWorldLocation, ValidLocation
{
	static ImmutableLocation of(final Location location)
	{
		if (location == null)
		{
			return new InvalidLocation("The location was null.");
		}

		else if (location.getWorld() == null)
		{
			return new NoWorldLocation("∅", new UUID(0, 0),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		}

		else if (!location.isWorldLoaded())
		{
			return new UnloadedWorldLocation(location.getWorld().getName(), location.getWorld().getUID(),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		}

		else {
			return new ValidLocation(location.getWorld().getName(), location.getWorld().getUID(),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		}
	}


	static ImmutableLocation of(String worldName, UUID worldUid, double x, double y, double z, float yaw, float pitch)
	{
		String checkedName = (worldName != null) ? worldName : "∅";
		checkedName = (!checkedName.isBlank()) ? checkedName : "⬚";

		return (Bukkit.getWorld(worldUid) == null)
				? new NoWorldLocation(checkedName, new UUID(0, 0), x, y, z, yaw, pitch)
				: new ValidLocation(worldName, worldUid, x, y, z, yaw, pitch);
	}

}
