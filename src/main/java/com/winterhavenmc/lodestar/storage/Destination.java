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

package com.winterhavenmc.lodestar.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.bukkit.ChatColor.stripColor;
import static org.bukkit.ChatColor.translateAlternateColorCodes;


public final class Destination {

	public enum Type {
		STORED,
		HOME,
		SPAWN,
	}

	private final Type type;
	private final String displayName;
	private final boolean worldValid;
	private final String worldName;
	private final UUID worldUid;
	private final double x;
	private final double y;
	private final double z;
	private final float yaw;
	private final float pitch;


	/**
	 * Class constructor
	 *
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(@Nonnull final String displayName, @Nonnull final Location location, @Nonnull Type type) {

		// validate parameters
		Objects.requireNonNull(displayName);
		Objects.requireNonNull(location);
		Objects.requireNonNull(type);

		this.type = type;
		this.displayName = displayName;

		if (location.getWorld() != null) {
			this.worldUid = location.getWorld().getUID();
			this.worldName = location.getWorld().getName();
			this.worldValid = true;
		}
		else {
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


	/**
	 * Class constructor used to create object fetched from data store
	 *
	 * @param displayName the destination display name
	 * @param worldValid  destination world valid
	 * @param worldName   destination world name
	 * @param worldUid    destination world uid
	 * @param x           destination x coordinate
	 * @param y           destination y coordinate
	 * @param z           destination z coordinate
	 * @param yaw         destination yaw
	 * @param pitch       destination pitch
	 */
	public Destination(final Type type,
	                   final String displayName,
	                   final boolean worldValid,
	                   final String worldName,
	                   final UUID worldUid,
	                   final double x,
	                   final double y,
	                   final double z,
	                   final float yaw,
	                   final float pitch) {

		this.type = type;
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
	public String toString() {
		return getDisplayName();
	}


	/**
	 * Check if destination is home location
	 *
	 * @return true if home, else false
	 */
	public boolean isHome() {
		return this.type.equals(Type.HOME);
	}


	/**
	 * Check if destination is spawn location
	 *
	 * @return true if destination is spawn, else false
	 */
	public boolean isSpawn() {
		return this.type.equals(Type.SPAWN);
	}


	/**
	 * Getter for destination key field
	 *
	 * @return the value of the key field
	 */
	String getKey() {
		return stripColor(translateAlternateColorCodes('&', displayName)).replace(' ', '_');
	}


	/**
	 * Getter for destination displayName field
	 *
	 * @return the value of the displayName field
	 */
	public String getDisplayName() {
		return displayName;
	}


	/**
	 * Getter for destination location
	 *
	 * @return {@link Optional} Location
	 */
	public Optional<Location> getLocation() {

		// if world uid is null, return empty optional
		if (worldUid == null) {
			return Optional.empty();
		}

		// get world by uid
		World world = Bukkit.getServer().getWorld(worldUid);

		// if world is null, return empty optional
		if (world == null) {
			return Optional.empty();
		}

		// return new location object for destination
		return Optional.of(new Location(world, x, y, z, yaw, pitch));
	}


	public UUID getWorldUid() {
		return worldUid;
	}


	public String getWorldName() {
		return worldName;
	}


	public boolean isValidWorld() {
		return worldValid;
	}


	public double getX() {
		return x;
	}


	public double getY() {
		return y;
	}


	public double getZ() {
		return z;
	}


	public float getYaw() {
		return yaw;
	}


	public float getPitch() {
		return pitch;
	}

}
