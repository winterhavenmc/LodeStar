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

package com.winterhavenmc.lodestar.plugin.util;

import com.winterhavenmc.lodestar.plugin.PluginMain;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;


/**
 * Responsible for sending various runtime metrics to bstats.org
 */
public class MetricsHandler
{
	/**
	 * Creates an instance of a MetricsHandler
	 *
	 * @param plugin reference to plugin main class
	 * @see <a href="https://bstats.org/plugin/bukkit/LodeStar">https://bstats.org/LodeStar</a>
	 */
	public MetricsHandler(PluginMain plugin) {

		Metrics metrics = new Metrics(plugin, 13927);

		// pie chart of configured language
		metrics.addCustomChart(new SimplePie("language", () -> plugin.getConfig().getString("language")));

		// pie chart of titles enabled
		metrics.addCustomChart(new SimplePie("titles_enabled", () -> plugin.getConfig().getString("titles-enabled")));

		// pie chart of particle effects enabled
		metrics.addCustomChart(new SimplePie("particle_effects", () -> plugin.getConfig().getString("particle-effects")));

		// pie chart of sound effects enabled
		metrics.addCustomChart(new SimplePie("sound_effects", () -> plugin.getConfig().getString("sound-effects")));

		// pie chart of from-nether enabled
		metrics.addCustomChart(new SimplePie("from_nether", () -> plugin.getConfig().getString("from-nether")));

		// pie chart of from-end enabled
		metrics.addCustomChart(new SimplePie("from_end", () -> plugin.getConfig().getString("from-end")));

		// pie chart of teleport cooldown time
		metrics.addCustomChart(new SimplePie("teleport_cooldown", () -> plugin.getConfig().getString("teleport-cooldown")));

		// pie chart of teleport warmup time
		metrics.addCustomChart(new SimplePie("teleport_warmup", () -> plugin.getConfig().getString("teleport-warmup")));
	}

}
