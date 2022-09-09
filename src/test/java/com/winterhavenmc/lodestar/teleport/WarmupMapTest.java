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

package com.winterhavenmc.lodestar.teleport;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.winterhavenmc.lodestar.PluginMain;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WarmupMapTest {

	private ServerMock server;
	private PluginMain plugin;

	@BeforeAll
	public void setUp() {
		// Start the mock server
		server = MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);
	}

	@AfterAll
	public void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
	}

	@Test
	void removePlayer() {

		// create mock player
		PlayerMock player = server.addPlayer();

		// put player in warmup map
		WarmupMap warmupMap = new WarmupMap(plugin);

		warmupMap.startPlayerWarmUp(player, 1);

		Assertions.assertTrue(warmupMap.containsPlayer(player), "Player was not inserted in warmup map.");

		warmupMap.removePlayer(player);

		Assertions.assertFalse(warmupMap.containsPlayer(player), "Player was not removed from warmup map.");
	}

	@Test
	void isWarmingUp() {
		// create mock player
		PlayerMock player = server.addPlayer();

		// put player in warmup map
		WarmupMap warmupMap = new WarmupMap(plugin);

		warmupMap.startPlayerWarmUp(player, 1);

		Assertions.assertTrue(warmupMap.isWarmingUp(player), "Warmup map does not contain player.");
	}

	@Test
	void startPlayerWarmUpTest() {

		// create mock player
		PlayerMock player = server.addPlayer();

		// put player in warmup map
		WarmupMap warmupMap = new WarmupMap(plugin);

		warmupMap.startPlayerWarmUp(player, 1);

		Assertions.assertTrue(warmupMap.containsPlayer(player), "Warmup map does not contain player.");
	}

	@Test
	void containsPlayer() {

		// create mock player
		PlayerMock player = server.addPlayer();

		// put player in warmup map
		WarmupMap warmupMap = new WarmupMap(plugin);

		warmupMap.startPlayerWarmUp(player, 1);

		Assertions.assertTrue(warmupMap.containsPlayer(player), "Warmup map does not contain player.");
	}

	@Test
	void getTaskId() {

		// create mock player
		PlayerMock player = server.addPlayer();

		// put player in warmup map
		WarmupMap warmupMap = new WarmupMap(plugin);

		warmupMap.startPlayerWarmUp(player, 1);

		Assertions.assertEquals(1, warmupMap.getTaskId(player), "Could not get taskId from warmup map.");
	}

	@Test
	void isInitiated() {
		// create mock player
		PlayerMock player = server.addPlayer();

		// put player in warmup map
		WarmupMap warmupMap = new WarmupMap(plugin);

		warmupMap.startPlayerWarmUp(player, 1);

		Assertions.assertFalse(warmupMap.isInitiated(player), "Player teleport is already initiated.");

		server.getScheduler().performTicks(plugin.getConfig().getLong("interact-delay"));

		Assertions.assertTrue(warmupMap.isInitiated(player), "Player teleport is not initiated.");
	}

}
