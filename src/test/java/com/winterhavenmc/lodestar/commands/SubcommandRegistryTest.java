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

package com.winterhavenmc.lodestar.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhavenmc.lodestar.PluginMain;
import org.junit.jupiter.api.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubcommandRegistryTest {

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
	void registerTest() {

		// instantiate SubcommandRegistry
		SubcommandRegistry registry = new SubcommandRegistry();

		// instantiate subcommand
		Subcommand subcommand = new TeleportSubcommand(plugin);

		// register subcommand in registry
		registry.register(subcommand);

		Assertions.assertTrue(registry.getSubcommand("tp").isPresent(), "registered subcommand is not present in registry.");
		Assertions.assertEquals(subcommand, registry.getSubcommand("tp").get(), "registered subcommand is not in registry.");
		Assertions.assertFalse(registry.getSubcommand("UnregisteredCommand").isPresent(), "unregistered command is present in registry.");
	}

	@Test
	void getSubcommandTest() {

		// instantiate SubcommandRegistry
		SubcommandRegistry registry = new SubcommandRegistry();

		// instantiate subcommand
		Subcommand subcommand = new StatusSubcommand(plugin);

		// register subcommand in registry
		registry.register(subcommand);

		Assertions.assertNotNull(registry.getSubcommand("status"), "registered subcommand is null.");
	}

	@Test
	void getKeysTest() {

		// instantiate SubcommandRegistry
		SubcommandRegistry registry = new SubcommandRegistry();

		// instantiate subcommand
		Subcommand subcommand = new StatusSubcommand(plugin);

		// register subcommand in registry
		registry.register(subcommand);

		// assert key for registered subcommand is in collection of keys
		Assertions.assertTrue(registry.getKeys().contains(subcommand.getName()), "registered subcommand key is not in registry.");
	}

}
