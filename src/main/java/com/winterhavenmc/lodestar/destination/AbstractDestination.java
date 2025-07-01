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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.bukkit.ChatColor.stripColor;
import static org.bukkit.ChatColor.translateAlternateColorCodes;


public sealed abstract class AbstractDestination permits HomeDestination, StoredDestination
{
	protected final String displayName;
	protected final boolean worldValid;
	protected final String worldName;
	protected final UUID worldUid;
	protected final double x;
	protected final double y;
	protected final double z;
	protected final float yaw;
	protected final float pitch;

	public AbstractDestination(final String displayName, final Location location)
	{
		// validate parameters
		Objects.requireNonNull(displayName);
		Objects.requireNonNull(location);

		this.displayName = displayName;

		if (location.getWorld() != null)
		{
			this.worldUid = location.getWorld().getUID();
			this.worldName = location.getWorld().getName();
			this.worldValid = true;
		}
		else
		{
			this.worldUid = null;
			this.worldName = "???";
			this.worldValid = false;
		}

		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public AbstractDestination(String displayName, boolean worldValid,
	                           String worldName, UUID worldUid, double x, double y, double z, float yaw, float pitch)
	{
		this.displayName = displayName;
		this.worldValid = worldValid;
		this.worldName = worldName;
		this.worldUid = worldUid;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;

	}

	/**
	 * Get string representation of destination
	 *
	 * @return String - destination display name
	 */
	@Override
	public String toString()
	{
		return getDisplayName();
	}

	/**
	 * Getter for destination key field
	 *
	 * @return the value of the key field
	 */
	public String key()
	{
		return stripColor(translateAlternateColorCodes('&', displayName)).replace(' ', '_');
	}

	/**
	 * Getter for destination displayName field
	 *
	 * @return the value of the displayName field
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Getter for destination location
	 *
	 * @return {@link Optional} Location
	 */
	public Optional<Location> location()
	{
		// if world uid is null, return empty optional
		if (worldUid == null)
		{
			return Optional.empty();
		}

		// get world by uid
		World world = Bukkit.getServer().getWorld(worldUid);

		// if world is null, return empty optional
		if (world == null)
		{
			return Optional.empty();
		}

		// return new location object for destination
		return Optional.of(new Location(world, x, y, z, yaw, pitch));
	}

	public UUID getWorldUid()
	{
		return worldUid;
	}

	public boolean isValidWorld()
	{
		return worldValid;
	}
}
