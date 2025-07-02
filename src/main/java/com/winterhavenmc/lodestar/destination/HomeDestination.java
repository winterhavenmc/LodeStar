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

package com.winterhavenmc.lodestar.destination;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;


/**
 * Record class that represents a Home destination
 */
public record HomeDestination(String displayName,
                              String worldName,
                              UUID worldUid,
                              double x,
                              double y,
                              double z,
                              float yaw,
                              float pitch) implements ValidDestination
{
	public HomeDestination(String displayName, Location location)
	{
		this(displayName, Objects.requireNonNull(location.getWorld()).getName(), location.getWorld().getUID(),
				location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

}
