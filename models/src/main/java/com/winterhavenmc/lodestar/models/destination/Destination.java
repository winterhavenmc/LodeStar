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

import com.winterhavenmc.library.messagebuilder.core.ports.pipeline.accessors.displayname.DisplayNameable;


/**
 * Represents an immutable location and a corresponding display name. A location is decomposed into validated,
 * immutable fields, and reconstituted into a Bukkit {@code Location} on access. Static factory methods are provided to
 * create new instances when reading from a datastore, or creating an instance from an existing Bukkit {@code Location}
 * and a corresponding {@code String} display name.
 */

public sealed interface Destination extends DisplayNameable permits ValidDestination, InvalidDestination
{
	String getDisplayName();
}
