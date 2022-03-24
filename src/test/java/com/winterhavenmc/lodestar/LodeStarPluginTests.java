package com.winterhavenmc.lodestar;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
	class PluginMainObjectTests {

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
			Assertions.assertNotNull(plugin.teleportHandler,
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
		@DisplayName("spawn star factory not null.")
		void SpawnStarFactoryNotNull() {
			Assertions.assertNotNull(plugin.lodeStarFactory,
					"spawn star factory is null.");
		}
	}

	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTests {

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



	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("Test Sounds config.")
	class SoundTests {

		// collection of enum sound name strings
		final Collection<String> enumSoundNames = new HashSet<>();

		// class constructor
		SoundTests() {
			// add all SoundId enum values to collection
			for (com.winterhavenmc.lodestar.sounds.SoundId SoundId : SoundId.values()) {
				enumSoundNames.add(SoundId.name());
			}
		}

		@SuppressWarnings("unused")
		Collection<String> GetConfigFileKeys() {
			return plugin.soundConfig.getSoundConfigKeys();
		}

		@ParameterizedTest
		@EnumSource(SoundId.class)
		@DisplayName("enum member soundId is contained in config file keys.")
		void FileKeysContainsEnumValue(SoundId soundId) {
			Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()),
					"Enum value soundId is not in config file keys.");
		}

		@ParameterizedTest
		@MethodSource("GetConfigFileKeys")
		@DisplayName("config file key has matching key in enum sound names")
		void SoundConfigEnumContainsAllFileSounds(String key) {
			Assertions.assertTrue(enumSoundNames.contains(key),
					"Enum SoundId does not contain config file key: " + key);
		}

		@ParameterizedTest
		@MethodSource("GetConfigFileKeys")
		@DisplayName("sound file key has valid bukkit sound name")
		void SoundConfigFileHasValidBukkitSound(String key) {
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
		void ItemNameNotIsSet() {
			Assertions.assertTrue(plugin.messageBuilder.getItemName().isPresent(),
					"item name is empty optional.");
		}

		@Test
		@DisplayName("item name is not null.")
		void ItemNamePluralIsSet() {
			Assertions.assertTrue(plugin.messageBuilder.getItemNamePlural().isPresent(),
					"item name plural is empty optional.");
		}

		@Test
		@DisplayName("item lore is not null.")
		void ItemLoreNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder.getItemLore(),
					"item lore is null.");
		}

		@Test
		@DisplayName("item lore is not null.")
		void ItemLoreNotEmpty() {
			Assertions.assertFalse(plugin.messageBuilder.getItemLore().isEmpty(),
					"item lore is empty.");
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@DisplayName("Test messages.")
	class MessageTests {

		// collection of enum sound name strings
		final Collection<String> enumMessageNames = new HashSet<>();

		// class constructor
		MessageTests() {
			// add all MessageId enum values to collection
			for (com.winterhavenmc.lodestar.messages.MessageId MessageId : MessageId.values()) {
				enumMessageNames.add(MessageId.name());
			}
		}

		@ParameterizedTest
		@EnumSource(MessageId.class)
		@DisplayName("enum member MessageId is contained in getConfig() keys.")
		void FileKeysContainsEnumValue(MessageId messageId) {
			Assertions.assertNotNull(messageId);
			Assertions.assertNotNull(plugin.messageBuilder.getMessage(messageId),
					"config file message is null.");
		}

	}


	@Nested
	@DisplayName("Test spawn star factory methods.")
	class SpawnStarFactoryTests {

		ItemStack lodeStarItem = plugin.lodeStarFactory.create("test destination");

		@Test
		@DisplayName("new item type is nether star.")
		void ItemSetDefaultType() {
			Assertions.assertEquals(Material.NETHER_STAR, lodeStarItem.getType(),
					"new item type is not nether star.");
		}

		@Test
		@DisplayName("new item name is SpawnStar.")
		void NewItemHasDefaultName() {
			Assertions.assertNotNull(lodeStarItem.getItemMeta(), "new item stack meta data is null.");
			Assertions.assertNotNull(lodeStarItem.getItemMeta().getDisplayName(),
					"new item stack display name meta data is null.");
			Assertions.assertEquals("LodeStar: test destination",
					ChatColor.stripColor(lodeStarItem.getItemMeta().getDisplayName()),
					"new item display name is not 'test destination'.");
		}

		@Test
		@DisplayName("new item has lore.")
		void NewItemHasDefaultLore() {
			Assertions.assertNotNull(lodeStarItem.getItemMeta());
			Assertions.assertNotNull(lodeStarItem.getItemMeta().getLore());
			Assertions.assertEquals("Use to teleport to test destination",
					ChatColor.stripColor(String.join(" ",
							lodeStarItem.getItemMeta().getLore())),
							"new item stack lore does not match default lore.");
		}

		@Test
		@DisplayName("new item is valid lode star item.")
		void CreateAndTestValidItem() {
			Assertions.assertTrue(plugin.lodeStarFactory.isItem(lodeStarItem),
					"new item stack is not a valid lode star item.");
		}
	}

}
