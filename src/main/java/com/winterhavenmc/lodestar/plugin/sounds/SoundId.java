/*
 * Copyright (c) 2022 Tim Savage.
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

package com.winterhavenmc.lodestar.plugin.sounds;

/**
 * Enum that contains entries for configured sounds
 */
public enum SoundId
{
	COMMAND_FAIL,
	COMMAND_INVALID,
	COMMAND_SUCCESS_GIVE_SENDER,
	COMMAND_SUCCESS_GIVE_TARGET,
	COMMAND_SUCCESS_DELETE,
	COMMAND_SUCCESS_DESTROY,
	COMMAND_SUCCESS_SET,
	COMMAND_SUCCESS_BIND,
	TELEPORT_DENIED_PERMISSION,
	TELEPORT_DENIED_WORLD_DISABLED,
	TELEPORT_CANCELLED,
	TELEPORT_CANCELLED_NO_ITEM,
	TELEPORT_WARMUP,
	TELEPORT_SUCCESS_DEPARTURE,
	TELEPORT_SUCCESS_ARRIVAL,
}
