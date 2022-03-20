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

package com.winterhavenmc.lodestar.util;

import com.winterhavenmc.lodestar.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public enum Config {

	DEBUG("debug"),
	LANGUAGE("language"),
	ENABLED_WORLDS("enabled-worlds"),
	DISABLED_WORLDS("disabled-worlds"),
	DEFAULT_MATERIAL("default-material"),
	DEFAULT_MATERIAL_ONLY("default-material-only"),
	MINIMUM_DISTANCE("minimum-distance"),
	TELEPORT_COOLDOWN("teleport-cooldown"),
	TELEPORT_WARMUP("teleport-warmup"),
	PARTICLE_EFFECTS("particle-effects"),
	SOUND_EFFECTS("sound-effects"),
	TITLES_ENABLED("titles-enabled"),
	LIGHTNING("lightning"),
	LEFT_CLICK("left-click"),
	SHIFT_CLICK("shift-click"),
	BEDSPAWN_FALLBACK("bedspawn-fallback"),
	REMOVE_FROM_INVENTORY("remove-from-inventory"),
	ALLOW_IN_RECIPES("allow-in-recipes"),
	CANCEL_ON_DAMAGE("cancel-on-damage"),
	CANCEL_ON_MOVEMENT("cancel-on-movement"),
	CANCEL_ON_INTERACTION("cancel-on-interaction"),
	INTERACT_DELAY("interact-delay"),
	MAX_GIVE_AMOUNT("max-give-amount"),
	FROM_NETHER("from-nether"),
	FROM_END("from-end"),
	LIST_PAGE_SIZE("list-page-size"),
	LOG_USE("log-use"),
	;

	final String key;

	final JavaPlugin plugin = JavaPlugin.getPlugin(PluginMain.class);

	Config(final String key) {
		this.key = key;
	}


	public String asString() {
		return plugin.getConfig().getString(this.key);
	}


	public Optional<String> asOptionalString() {
		return Optional.ofNullable(plugin.getConfig().getString(this.key));
	}


	public int asInt() {
		return plugin.getConfig().getInt(this.key);
	}


	public boolean isTrue() {
		return plugin.getConfig().getBoolean(this.key);
	}

}
