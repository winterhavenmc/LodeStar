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

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.storage.Destination;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


class SpawnTeleporter extends AbstractTeleporter implements Teleporter {

	private final TeleportExecutor teleportExecutor;


	SpawnTeleporter(final PluginMain plugin, final TeleportExecutor teleportExecutor) {
		super(plugin);
		this.teleportExecutor = teleportExecutor;
	}


	/**
	 * Begin teleport to world spawn destination
	 *
	 * @param player the player to teleport
	 */
	@Override
	public void initiate(final Player player) {

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// get spawn destination
		Optional<Destination> optionalDestination = getSpawnDestination(player);

		if (optionalDestination.isPresent()) {

			// get location from destination
			Location location = optionalDestination.get().getLocation();

			// if from-nether is enabled in config and player is in nether, try to get overworld spawn location
			if (plugin.getConfig().getBoolean("from-nether") && isInNetherWorld(player)) {
				location = getOverworldSpawnLocation(player).orElse(location);
			}
			// if from-end is enabled in config and player is in end, try to get overworld spawn location
			else if (plugin.getConfig().getBoolean("from-end") && isInEndWorld(player)) {
				location = getOverworldSpawnLocation(player).orElse(location);
			}

			// if remove-from-inventory is configured on-use, take one LodeStar item from inventory now
			removeFromInventoryOnUse(player, playerItem);

			// create final destination object
			Destination finalDestination = new Destination(plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn"), location);

			// initiate delayed teleport for player to final destination
			execute(player, finalDestination, playerItem, MessageId.TELEPORT_WARMUP_SPAWN);
		}
		else {
			// send invalid destination message
			plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
					.setMacro(Macro.DESTINATION, plugin.messageBuilder.getSpawnDisplayName())
					.send();
		}
	}


	@Override
	public void execute(final Player player, final Destination finalDestination, final ItemStack playerItem, final MessageId messageId) {
		teleportExecutor.execute(player, finalDestination, playerItem, messageId);
	}


	/**
	 * Get overworld spawn location corresponding to a player nether or end world.
	 *
	 * @param player the passed player whose current world will be used to find a matching over world spawn location
	 * @return {@link Optional} wrapped spawn location of the normal world associated with the passed player
	 * nether or end world, or the current player world spawn location if no matching normal world found
	 */
	protected Optional<Location> getOverworldSpawnLocation(final Player player) {

		// check for null parameter
		if (player == null) {
			return Optional.empty();
		}

		// create list to store normal environment worlds
		List<World> normalWorlds = new ArrayList<>();

		// iterate through all server worlds
		for (World checkWorld : plugin.getServer().getWorlds()) {

			// if world is normal environment, try to match name to passed world
			if (checkWorld.getEnvironment().equals(World.Environment.NORMAL)) {

				// check if normal world matches passed world minus nether/end suffix
				if (checkWorld.getName().equals(player.getWorld().getName().replaceFirst("(_nether$|_the_end$)", ""))) {
					return Optional.of(plugin.worldManager.getSpawnLocation(checkWorld));
				}

				// if no match, add to list of normal worlds
				normalWorlds.add(checkWorld);
			}
		}

		// if only one normal world exists, return that world
		if (normalWorlds.size() == 1) {
			return Optional.of(plugin.worldManager.getSpawnLocation(normalWorlds.get(0)));
		}

		// if no matching normal world found and more than one normal world exists, return passed world spawn location
		return Optional.of(plugin.worldManager.getSpawnLocation(player.getWorld()));
	}


	/**
	 * Check if a player is in a nether world
	 *
	 * @param player the player
	 * @return true if player is in a nether world, false if not
	 */
	protected boolean isInNetherWorld(final Player player) {
		return player.getWorld().getEnvironment().equals(World.Environment.NETHER);
	}


	/**
	 * Check if a player is in an end world
	 *
	 * @param player the player
	 * @return true if player is in an end world, false if not
	 */
	protected boolean isInEndWorld(final Player player) {
		return player.getWorld().getEnvironment().equals(World.Environment.THE_END);
	}

}
