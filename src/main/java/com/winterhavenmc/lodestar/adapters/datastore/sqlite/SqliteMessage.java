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

package com.winterhavenmc.lodestar.adapters.datastore.sqlite;

public enum SqliteMessage
{
	DATASTORE_INITIALIZED_NOTICE("SQLite datastore initialized."),
	DATASTORE_INITIALIZED_ERROR("The SQLite datastore is already initialized."),
	DATASTORE_CLOSE_ERROR("An error occurred while closing the SQLite datastore."),
	DATASTORE_CLOSED_NOTICE("The SQLite datastore connection was successfully closed."),

	SELECT_ALL_KEYS_ERROR("An error occurred while trying to fetch all keys from the SQLite datastore."),
	SELECT_ALL_RECORDS_ERROR("An error occurred while trying to select all records from the SQLite datastore."),
	SELECT_RECORD_ERROR("An error occurred while selecting a destination record from the SQLite datastore."),

	INSERT_RECORD_ERROR("An error occurred while inserting a destination record into the SQLite datastore."),

	DELETE_RECORD_ERROR("An error occurred while attempting to delete a destination record from the SQLite datastore."),
	;

	private final String defaultMessage;


	SqliteMessage(final String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	public String getDefaultMessage()
	{
		return this.defaultMessage;
	}


	public String getLocalizedMessage()
	{
		return this.defaultMessage;
	}
}
