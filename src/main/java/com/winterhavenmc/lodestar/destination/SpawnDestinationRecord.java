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

import java.util.Optional;
import java.util.UUID;


/**
 * Class constructor used to create object fetched from data store
 *
 * @param displayName the destination display name
 * @param worldValid  destination world valid
 * @param worldName   destination world name
 * @param worldUid    destination world uid
 * @param x           destination x coordinate
 * @param y           destination y coordinate
 * @param z           destination z coordinate
 * @param yaw         destination yaw
 * @param pitch       destination pitch
 */
public record SpawnDestinationRecord(String displayName,
                                     boolean worldValid,
                                     String worldName,
                                     UUID worldUid,
                                     double x,
                                     double y,
                                     double z,
                                     float yaw,
                                     float pitch) implements ValidDestination
{
	public SpawnDestinationRecord(String displayName, Location location)
	{
		this(displayName, true, location.getWorld().getName(), location.getWorld().getUID(),
				location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	/**
	 * Getter for destination key field
	 *
	 * @return the value of the key field
	 */
	@Override
	public String key()
	{
		return ValidDestination.super.key();
	}

	/**
	 * Getter for destination location
	 *
	 * @return {@link Optional} Location
	 */
	@Override
	public Optional<Location> location()
	{
		return ValidDestination.super.location();
	}

	@Override
	public boolean isValidWorld()
	{
		return false;
	}
}
