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

import com.winterhavenmc.library.messagebuilder.pipeline.adapters.location.Locatable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Represents a valid Bukkit {@link Location}, using only immutable fields.
 * <p>
 * <strong>Note:</strong> While this type represents a Location that was valid when created, it can offer no
 * guarantee that the location's world has not been subsequently unloaded or deleted.
 */
public record ValidLocation(String worldName,
                            UUID worldUid,
                            double x,
                            double y,
                            double z,
                            float yaw,
                            float pitch) implements ImmutableLocation, Locatable
{
	/**
	 * Return the world for this location.
	 *
	 * @return instance of a Bukkit {@link World}, or {@code null} if the location's world has been deleted
	 * after creation of this instance.
	 */
	public World world()
	{
		return Bukkit.getWorld(worldUid);
	}


	/**
	 * Return a Bukkit {@link Location} represented by this {@link ImmutableLocation}.
	 *
	 * @return instance of a Bukkit {@link Location}
	 */
	public Location getLocation()
	{
		return (Bukkit.getWorld(worldUid) != null)
				? new Location(Bukkit.getWorld(worldUid), x, y, z, yaw, pitch)
				: null;
	}
}
