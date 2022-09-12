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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.winterhavenmc.util.TimeUnit.SECONDS;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CooldownMapTest {

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
	void startPlayerCooldownTest() {

		// instantiate cooldown map instance
		CooldownMap cooldownMap = new CooldownMap(plugin);

		// create mock player
		PlayerMock player = server.addPlayer();

		// insert player in cooldown map
		cooldownMap.startPlayerCooldown(player);

		// assert player is in cooldown map
		assertTrue(cooldownMap.isCoolingDown(player), "player is not in cooldown map.");
	}


	@Test
	void expiringPlayerCooldownTest() {

		// instantiate cooldown map instance
		CooldownMap cooldownMap = new CooldownMap(plugin);

		// create mock player
		PlayerMock player = server.addPlayer();

		// insert player in cooldown map
		cooldownMap.startPlayerCooldown(player);

		// assert player is in cooldown map
		assertTrue(cooldownMap.isCoolingDown(player), "player is not in cooldown map.");
	}


	@Test
	void cancelPlayerCooldownTest() {

		// instantiate cooldown map instance
		CooldownMap cooldownMap = new CooldownMap(plugin);

		// create mock player
		PlayerMock player = server.addPlayer();

		// insert player in cooldown map
		cooldownMap.startPlayerCooldown(player);

		// remove player from cooldown map
		cooldownMap.removePlayer(player);

		assertFalse(cooldownMap.isCoolingDown(player), "Player is still in cooldown map after removal.");
	}

	@Test
	void getCooldownTimeRemainingTest() {

		// instantiate cooldown map instance
		CooldownMap cooldownMap = new CooldownMap(plugin);

		// create mock player
		PlayerMock player = server.addPlayer();

		// insert player in cooldown map
		cooldownMap.startPlayerCooldown(player);

		assertTrue(cooldownMap.getCooldownTimeRemaining(player) > 0, "Cooldown time is not greater than 0.");
		assertTrue(cooldownMap.getCooldownTimeRemaining(player) <= SECONDS.toMillis(plugin.getConfig().getLong("teleport-cooldown")), "Cooldown time is not less than or equal to config setting.");
	}

	@Test
	void isCoolingDownTest() {

		// instantiate cooldown map instance
		CooldownMap cooldownMap = new CooldownMap(plugin);

		// create mock player
		PlayerMock player = server.addPlayer();

		// insert player in cooldown map
		cooldownMap.startPlayerCooldown(player);

		// assert player is in cooldown map
		assertTrue(cooldownMap.isCoolingDown(player), "player is not in cooldown map.");
	}
}
