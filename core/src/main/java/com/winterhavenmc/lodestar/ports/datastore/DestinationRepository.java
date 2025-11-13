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

package com.winterhavenmc.lodestar.ports.datastore;

import com.winterhavenmc.lodestar.models.destination.Destination;
import com.winterhavenmc.lodestar.models.destination.StoredDestination;

import java.util.Collection;
import java.util.List;


public interface DestinationRepository
{
	/**
	 * Get record
	 *
	 * @param destinationName the name string key of the destination to be retrieved from the datastore
	 * @return destination object or null if no matching record
	 */
	Destination get(final String destinationName);


	/**
	 * Insert a collection of records
	 * @param storedDestinations a collection of records to be inserted
	 *
	 * @return count of records inserted
	 */
	int save(final Collection<StoredDestination> storedDestinations);


	/**
	 * get all display names
	 *
	 * @return List of all destination display name strings
	 */
	List<String> names();


	/**
	 * Delete record
	 *
	 * @param destinationName the name key string of the destination record to be deleted
	 * @return the destination record that was deleted
	 */
	@SuppressWarnings("UnusedReturnValue")
	Destination delete(final String destinationName);

}
