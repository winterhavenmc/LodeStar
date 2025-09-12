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

import org.jetbrains.annotations.NotNull;


/**
 * Record class that represents a Home destination
 */
public record HomeDestination(String displayName) implements ValidDestination
{
	public static Destination of(final String displayName)
	{
		if (displayName == null) return new InvalidDestination("NULL", "The displayName parameter was null.");
		else if (displayName.isBlank()) return new InvalidDestination("BLANK", "The displayName parameter was blank.");
		else return new HomeDestination(displayName);
	}

	@Override
	public @NotNull String toString()
	{
		return(displayName);
	}
}
