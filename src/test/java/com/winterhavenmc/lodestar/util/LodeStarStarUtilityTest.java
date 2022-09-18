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

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhavenmc.lodestar.PluginMain;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LodeStarStarUtilityTest {

	@SuppressWarnings({"FieldCanBeLocal", "unused"})
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
	void createTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertNotNull(testItem, "create produced null item.");
		assertEquals(testItem.getType(), Material.NETHER_STAR, "item is not default material type.");
		assertEquals(1, testItem.getAmount(), "item stack is not 1.");
	}

	@Test
	void maxGiveAmountTest() {
		plugin.getConfig().set("max-give-amount", 64);
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertNotNull(testItem, "create produced null item.");
		assertEquals(testItem.getType(), Material.NETHER_STAR, "item is not default material type.");
		assertEquals(1, testItem.getAmount(), "item stack is not 1.");
	}

	@Test
	void isItemTest() {

		// create test item
		ItemStack testItem = plugin.lodeStarUtility.create("Home");

		// assert test item passes isItem tests
		assertTrue(plugin.lodeStarUtility.isItem(testItem), "testItem is not a proper spawn star item.");

		// assert null ItemStack is does not pass isItem test
		assertFalse(plugin.lodeStarUtility.isItem(null), "null ItemStack passed isItem test.");

		// create item without metadata
		ItemStack itemStack = new ItemStack(Material.DIRT);
		assertFalse(plugin.lodeStarUtility.isItem(itemStack), "item stack without metadata passed isItem test.");
	}

	@Test
	void getDefaultItemStackTest() {

		// get item of default material
		ItemStack testItem = plugin.lodeStarUtility.getDefaultItemStack();

		assertEquals(testItem.getType(), Material.NETHER_STAR, "item stack is not NETHER_STAR.");
	}

	@Test
	void setMetaDataTest() {

		// create test item and set metadata
		ItemStack testItem = new ItemStack(Material.DIRT);

		plugin.lodeStarUtility.setMetaData(testItem, "Home");

		ItemMeta itemMeta = testItem.getItemMeta();

		assertNotNull(itemMeta, "test item metadata is null");
		assertNotNull(itemMeta.getDisplayName(), "item metadata display name is null.");
		assertFalse(itemMeta.getDisplayName().isBlank(), "item metadata display name is blank.");
		assertEquals("LodeStar: Home", ChatColor.stripColor(testItem.getItemMeta().getDisplayName()), "new item display name is not 'LodeStar: Home'.");
		assertNotNull(itemMeta.getLore(), "item lore is null.");
		assertFalse(itemMeta.getLore().isEmpty(), "item lore is empty.");
		assertEquals("Use to teleport to Home", ChatColor.stripColor(String.join(" ", itemMeta.getLore())), "new item stack lore does not match default lore.");
	}

	@Test
	void getHomeDisplayNameTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertEquals("Home", plugin.lodeStarUtility.getDisplayName(testItem).orElse(null), "item destination name is not 'Home'");
	}

	@Test
	void getSpawnDisplayNameTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Spawn");
		assertEquals("Spawn", plugin.lodeStarUtility.getDisplayName(testItem).orElse(null), "item destination name is not 'Spawn'");
	}

	@Test
	void getNullKeyDisplayNameTest() {
		assertTrue(plugin.lodeStarUtility.getDisplayName((String) null).isEmpty(), "null key did not return empty optional.");
	}

	@Test
	void getNullItemDisplayNameTest() {
		assertTrue(plugin.lodeStarUtility.getDisplayName((ItemStack) null).isEmpty(), "null item stack did not return empty optional.");
	}

	@Test
	void getKeyTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertEquals("Home", plugin.lodeStarUtility.getKey(testItem), "Item key is not 'Home'");
	}

	@Test
	void isDefaultItemTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		ItemStack falseItem = new ItemStack(Material.DIRT);
		assertTrue(plugin.lodeStarUtility.isDefaultItem(testItem), "Item is not default item.");
		assertFalse(plugin.lodeStarUtility.isDefaultItem(null), "null item matched default item.");
		assertFalse(plugin.lodeStarUtility.isDefaultItem(falseItem), "false item matched for default item.");
	}

	@Test
	void deriveKeyTest() {
		assertEquals("Destination_Name", plugin.lodeStarUtility.deriveKey("&aDestination Name"), "derived key is not 'Destination_Name'");
		assertEquals("", plugin.lodeStarUtility.deriveKey(null), "derived key from null object is not blank.");
	}

	@Test
	void destinationExistsTest() {
		assertTrue(plugin.lodeStarUtility.destinationExists("home"));
		assertTrue(plugin.lodeStarUtility.destinationExists("spawn"));
		assertFalse(plugin.lodeStarUtility.destinationExists(null));
		assertFalse(plugin.lodeStarUtility.destinationExists(""));
	}

}
