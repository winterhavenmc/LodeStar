package com.winterhavenmc.lodestar;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

import com.winterhavenmc.lodestar.sounds.SoundId;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LodeStarPluginTests {

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
	class MockingTests {

		@Test
		@DisplayName("server is not null.")
		void serverNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("plugin is not null.")
		void pluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null.");
		}

		@Test
		@DisplayName("plugin is enabled.")
		void pluginEnabled() {
			Assertions.assertTrue(plugin.isEnabled(),"plugin is not enabled.");
		}
	}

	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginMainObjectTests {

		@Test
		@DisplayName("language handler not null.")
		void messageBuilderNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder, "language handler is null.");
		}

		@Test
		@DisplayName("sound config not null.")
		void soundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig, "sound config is null.");
		}

		@Test
		@DisplayName("teleport manager not null.")
		void teleportManagerNotNull() {
			Assertions.assertNotNull(plugin.teleportHandler, "teleport manager is null.");
		}

		@Test
		@DisplayName("world manager not null.")
		void worldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager, "world manager is null.");
		}

		@Test
		@DisplayName("command manager not null.")
		void commandManagerNotNull() {
			Assertions.assertNotNull(plugin.commandManager, "command manager is null.");
		}

		@Test
		@DisplayName("spawn star factory not null.")
		void spawnStarFactoryNotNull() {
			Assertions.assertNotNull(plugin.lodeStarUtility, "spawn star factory is null.");
		}
	}

	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTests {

		@Test
		@DisplayName("config not null.")
		void configNotNull() {
			Assertions.assertNotNull(plugin.getConfig(), "plugin config is null.");
		}

		@Test
		@DisplayName("test configured language.")
		void getLanguage() {
			Assertions.assertEquals("en-US", plugin.getConfig().getString("language"), "configured language does not equal default 'en-US'");
		}
	}


	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("Test Sounds config.")
	class SoundTests {

		// collection of enum sound name strings
		final Collection<String> enumSoundNames = new HashSet<>();

		// class constructor
		SoundTests() {
			// add all SoundId enum values to collection
			for (SoundId SoundId : SoundId.values()) {
				enumSoundNames.add(SoundId.name());
			}
		}

		Collection<String> getConfigFileKeys() {
			return plugin.soundConfig.getSoundConfigKeys();
		}

		@ParameterizedTest
		@EnumSource(SoundId.class)
		@DisplayName("enum member soundId is contained in config file keys.")
		void fileKeysContainsEnumValue(SoundId soundId) {
			Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()),
					"Enum value soundId is not in config file keys.");
		}

		@ParameterizedTest
		@MethodSource("getConfigFileKeys")
		@DisplayName("config file key has matching key in enum sound names")
		void soundConfigEnumContainsAllFileSounds(String key) {
			Assertions.assertTrue(enumSoundNames.contains(key),
					"Enum SoundId does not contain config file key: " + key);
		}

		@ParameterizedTest
		@MethodSource("getConfigFileKeys")
		@DisplayName("sound file key has valid bukkit sound name")
		void soundConfigFileHasValidBukkitSound(String key) {
			String bukkitSoundName = plugin.soundConfig.getBukkitSoundName(key);
			Assertions.assertTrue(plugin.soundConfig.isValidBukkitSoundName(bukkitSoundName),
					"file key '" + key + "' has invalid bukkit sound name: " + bukkitSoundName);
			System.out.println("File key '" + key + "' has valid bukkit sound name: " + bukkitSoundName);
		}
	}


	@Nested
	@DisplayName("test message builder.")
	class MessageBuilderTests {

		@Test
		@DisplayName("item name is not null.")
		void itemNameNotIsSet() {
			Assertions.assertTrue(plugin.messageBuilder.getItemName().isPresent(),
					"item name is empty optional.");
		}

		@Test
		@DisplayName("item name is not null.")
		void itemNamePluralIsSet() {
			Assertions.assertTrue(plugin.messageBuilder.getItemNamePlural().isPresent(),
					"item name plural is empty optional.");
		}

		@Test
		@DisplayName("item lore is not null.")
		void itemLoreNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder.getItemLore(),
					"item lore is null.");
		}

		@Test
		@DisplayName("item lore is not null.")
		void itemLoreNotEmpty() {
			Assertions.assertFalse(plugin.messageBuilder.getItemLore().isEmpty(),
					"item lore is empty.");
		}
	}

}
