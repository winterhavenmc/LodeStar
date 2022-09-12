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

		// create test item
		ItemStack testItem = plugin.lodeStarUtility.create("Home");

		// assert test item is not null
		assertNotNull(testItem, "create produced null item.");
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
	void getDestinationNameTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertEquals("Home", plugin.lodeStarUtility.getDestinationName(testItem), "item destination name does is not 'Home'");
	}

	@Test
	void getKeyTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertEquals("Home", plugin.lodeStarUtility.getKey(testItem), "Item destiname key is not 'Home'");
	}

	@Test
	void isDefaultItemTest() {
		ItemStack testItem = plugin.lodeStarUtility.create("Home");
		assertTrue(plugin.lodeStarUtility.isDefaultItem(testItem), "Item is not default item.");

		ItemStack falseItem = new ItemStack(Material.DIRT);
		assertFalse(plugin.lodeStarUtility.isDefaultItem(falseItem), "false item matched for default item.");

		assertFalse(plugin.lodeStarUtility.isDefaultItem(null), "null item matched default item.");
	}

	@Test
	void deriveKeyTest() {
		assertEquals("Destination_Name", plugin.lodeStarUtility.deriveKey("&aDestination Name"), "derived key is not 'Destination_Name'");
		assertEquals("", plugin.lodeStarUtility.deriveKey(null), "derived key from null object is not blank.");
	}

}
