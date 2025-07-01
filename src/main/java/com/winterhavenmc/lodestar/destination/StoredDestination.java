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

import javax.annotation.Nonnull;
import java.util.UUID;


public final class StoredDestination extends AbstractDestination
{
	/**
	 * Class constructor
	 *
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	StoredDestination(@Nonnull final String displayName, @Nonnull final Location location)
	{
		super(displayName, location);
	}


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
	StoredDestination(final String displayName,
	                  final boolean worldValid,
	                  final String worldName,
	                  final UUID worldUid,
	                  final double x,
	                  final double y,
	                  final double z,
	                  final float yaw,
	                  final float pitch)
	{
		super(displayName, worldValid, worldName, worldUid, x, y, z, yaw, pitch);
	}

}
