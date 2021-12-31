package com.winterhaven_mc.lodestar;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginMainTests {

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

	@Nested
	@DisplayName("Test mocking setup.")
	class Mocking {

		@Test
		@DisplayName("server is not null.")
		void ServerNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("plugin is not null.")
		void PluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null.");
		}

		@Test
		@DisplayName("plugin is enabled.")
		void PluginEnabled() {
			Assertions.assertTrue(plugin.isEnabled(),"plugin is not enabled.");
		}
	}

	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginMainObjects {

		@Test
		@DisplayName("language handler not null.")
		void LanguageHandlerNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder,
					"language handler is null.");
		}

		@Test
		@DisplayName("sound config not null.")
		void SoundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig,
					"sound config is null.");
		}

		@Test
		@DisplayName("teleport manager not null.")
		void TeleportManagerNotNull() {
			Assertions.assertNotNull(plugin.teleportManager,
					"teleport manager is null.");
		}

		@Test
		@DisplayName("world manager not null.")
		void WorldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager,
					"world manager is null.");
		}

		@Test
		@DisplayName("command manager not null.")
		void commandManagerNotNull() {
			Assertions.assertNotNull(plugin.commandManager,
					"command manager is null.");
		}

		@Test
		@DisplayName("player event listener not null.")
		void PlayerEventListenerNotNull() {
			Assertions.assertNotNull(plugin.playerEventListener,
					"player event listener is null.");
		}

		@Test
		@DisplayName("spawn star factory not null.")
		void SpawnStarFactoryNotNull() {
			Assertions.assertNotNull(plugin.lodeStarFactory,
					"spawn star factory is null.");
		}
	}

	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class Config {

		@Test
		@DisplayName("config not null.")
		void ConfigNotNull() {
			Assertions.assertNotNull(plugin.getConfig(),
					"plugin config is null.");
		}

		@Test
		@DisplayName("test configured language.")
		void GetLanguage() {
			Assertions.assertEquals("en-US", plugin.getConfig().getString("language"),
					"language does not equal 'en-US'");
		}
	}

}
