package com.winterhaven_mc.lodestar.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhaven_mc.lodestar.PluginMain;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LodeStarFactoryTests {

	private ServerMock server;
	//    private WorldMock world;
	private PluginMain plugin;

//	private String testDestination = "test destination";

	@BeforeAll
	public void setUp() {
		// Start the mock server
		server = MockBukkit.mock();

		// create mock world
//        world = server.addSimpleWorld("world");

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
		@DisplayName("mock server is not null.")
		void MockServerNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("mock plugin is not null.")
		void MockPluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null");
		}
	}

	@Nested
	@DisplayName("Test spawn star factory methods.")
	class SpawnStarFactory {

//		ItemStack lodeStarItem = plugin.lodeStarFactory.create("test destination");

//		@Test
//		@DisplayName("new item type is nether star.")
//		void ItemSetDefaultType() {
//			Assertions.assertEquals(Material.NETHER_STAR, lodeStarItem.getType(),
//					"new item type is not nether star.");
//		}

//		@Test
//		@DisplayName("new item name is SpawnStar.")
//		void NewItemHasDefaultName() {
//			Assertions.assertNotNull(lodeStarItem.getItemMeta(), "new item stack meta data is null.");
//			Assertions.assertNotNull(lodeStarItem.getItemMeta().getDisplayName(),
//					"new item stack display name meta data is null.");
//			Assertions.assertEquals("test destination",
//					ChatColor.stripColor(lodeStarItem.getItemMeta().getDisplayName()),
//					"new item display name is not 'test destination'.");
//		}

//		@Test
//		@DisplayName("new item has lore.")
//		void NewItemHasDefaultLore() {
//			Assertions.assertNotNull(lodeStarItem.getItemMeta());
//			Assertions.assertNotNull(lodeStarItem.getItemMeta().getLore());
//			Assertions.assertEquals("Use to Return to World Spawn",
//					ChatColor.stripColor(String.join(" ",
//							spawnStarItem.getItemMeta().getLore())),"" +
//							"new item stack lore does not match default lore.");
//		}

//		@Test
//		@DisplayName("new item is valid lode star item.")
//		void CreateAndTestValidItem() {
//			Assertions.assertTrue(plugin.lodeStarFactory.isItem(lodeStarItem),
//					"new item stack is not a valid lode star item.");
//		}

		@Test
		@DisplayName("lode star factory is not null after reload.")
		void ReloadSpawnStarFactory() {
			plugin.lodeStarFactory.reload();
			Assertions.assertNotNull(plugin.lodeStarFactory, "spawn star factory is null after reload.");
		}
	}
}
