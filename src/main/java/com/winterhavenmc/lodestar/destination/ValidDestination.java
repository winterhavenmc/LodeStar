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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;
import java.util.UUID;

import static org.bukkit.ChatColor.stripColor;
import static org.bukkit.ChatColor.translateAlternateColorCodes;


public sealed interface ValidDestination extends Destination permits HomeDestination, SpawnDestination, StoredDestination
{
	String displayName();
	String worldName();
	UUID worldUid();
	double x();
	double y();
	double z();
	float yaw();
	float pitch();


	/**
	 * Getter for destination key field
	 *
	 * @return the value of the key field
	 */
	default String key()
	{
		return stripColor(translateAlternateColorCodes('&', this.displayName())).replace(' ', '_');
	}


	/**
	 * Getter for destination location
	 *
	 * @return {@link Optional} Location
	 */
	default Optional<Location> location()
	{
		// if world uid is null, return empty optional
		if (this.worldUid() == null)
		{
			return Optional.empty();
		}

		// get world by uid
		World world = Bukkit.getServer().getWorld(this.worldUid());

		// if world is null, return empty optional
		if (world == null)
		{
			return Optional.empty();
		}

		// return new location object for destination
		return Optional.of(new Location(world, this.x(), this.y(), this.z(), this.yaw(), this.pitch()));
	}

}
