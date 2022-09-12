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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.winterhavenmc.util.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemovePlayerCooldownTaskTest {

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
	void playerCooldownTest() {

		// create mock player
		PlayerMock player = server.addPlayer();

		// insert player in cooldown map
		plugin.teleportHandler.startPlayerCooldown(player);

		// advance server ticks by configured cooldown period
		server.getScheduler().performTicks(SECONDS.toMillis(plugin.getConfig().getLong("teleport-cooldown")));

		// assert player is no longer in cooldown map
		assertFalse(plugin.teleportHandler.isCoolingDown(player), "Player is still cooling after cooldown period.");
	}

}
