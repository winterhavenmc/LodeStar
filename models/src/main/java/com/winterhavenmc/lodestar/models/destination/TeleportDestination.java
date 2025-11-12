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

package com.winterhavenmc.lodestar.models.destination;

import com.winterhavenmc.library.messagebuilder.core.ports.pipeline.accessors.location.Locatable;
import com.winterhavenmc.lodestar.models.location.ValidLocation;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;


public record TeleportDestination(String displayName, ValidLocation location) implements ValidDestination, Locatable
{
	public static Destination of(final ValidDestination validDestination, final ValidLocation validLocation)
	{
		if (validDestination == null) return new InvalidDestination("NULL", "The validDestination parameter was null");
		else if (validLocation == null) return new InvalidDestination(validDestination.displayName(), "The validLocation parameter was null.");
		else return new TeleportDestination(validDestination.displayName(), validLocation);
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
