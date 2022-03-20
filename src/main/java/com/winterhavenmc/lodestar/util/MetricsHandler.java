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
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public class MetricsHandler {

	public MetricsHandler(PluginMain plugin) {

		Metrics metrics = new Metrics(plugin, 13927);

		// pie chart of configured language
		metrics.addCustomChart(new SimplePie("language", Config.LANGUAGE::asString));

		// pie chart of titles enabled
		metrics.addCustomChart(new SimplePie("titles_enabled", Config.TITLES_ENABLED::asString));

		// pie chart of particle effects enabled
		metrics.addCustomChart(new SimplePie("particle_effects", Config.PARTICLE_EFFECTS::asString));

		// pie chart of sound effects enabled
		metrics.addCustomChart(new SimplePie("sound_effects", Config.SOUND_EFFECTS::asString));

		// pie chart of from-nether enabled
		metrics.addCustomChart(new SimplePie("from_nether", Config.FROM_NETHER::asString));

		// pie chart of from-end enabled
		metrics.addCustomChart(new SimplePie("from_end", Config.FROM_END::asString));

		// pie chart of teleport cooldown time
		metrics.addCustomChart(new SimplePie("teleport_cooldown", Config.TELEPORT_COOLDOWN::asString));

		// pie chart of teleport warmup time
		metrics.addCustomChart(new SimplePie("teleport_warmup", Config.TELEPORT_WARMUP::asString));

	}

}
