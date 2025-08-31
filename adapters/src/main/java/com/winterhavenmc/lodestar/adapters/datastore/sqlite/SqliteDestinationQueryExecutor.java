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

import com.winterhavenmc.lodestar.models.destination.ValidDestination;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SqliteDestinationQueryExecutor
{
	int insertRecords(final ValidDestination validDestination, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, validDestination.key());
		preparedStatement.setString(2, validDestination.displayName());
		preparedStatement.setString(3, validDestination.location().worldName());
		preparedStatement.setLong(  4, validDestination.location().worldUid().getMostSignificantBits());
		preparedStatement.setLong(  5, validDestination.location().worldUid().getLeastSignificantBits());
		preparedStatement.setDouble(6, validDestination.location().x());
		preparedStatement.setDouble(7, validDestination.location().y());
		preparedStatement.setDouble(8, validDestination.location().z());
		preparedStatement.setFloat( 9, validDestination.location().yaw());
		preparedStatement.setFloat(10, validDestination.location().pitch());
		return preparedStatement.executeUpdate();
	}


	int deleteRecords(final String key, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, key);
		return preparedStatement.executeUpdate();
	}


	ResultSet selectKeys(final PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.executeQuery();
	}

}
