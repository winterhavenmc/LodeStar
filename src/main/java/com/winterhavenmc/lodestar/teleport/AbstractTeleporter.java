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
import com.winterhavenmc.lodestar.destination.DestinationType;
import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.destination.Destination;
import com.winterhavenmc.lodestar.destination.InvalidDestination;
import com.winterhavenmc.lodestar.destination.ValidDestination;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * This class provides methods that are common to the concrete Teleporter classes.
 * It is not intended to be subclassed, except to provide these common methods to
 * the Teleporter classes within this package. The methods are declared final to
 * prevent them being overridden, and the class and methods are declared package-private
 * to prevent their use outside this package.
 */
abstract non-sealed class AbstractTeleporter implements Teleporter
{
	protected final PluginMain plugin;
	protected final TeleportExecutor teleportExecutor;


	AbstractTeleporter(final PluginMain plugin, final TeleportExecutor teleportExecutor)
	{
		this.plugin = plugin;
		this.teleportExecutor = teleportExecutor;
	}


	/**
	 * Execute the teleport to validDestination
	 *
	 * @param player      the player to teleport
	 * @param validDestination the validDestination
	 * @param messageId   the teleport warmup message to send to player
	 */
	@Override
	public void execute(final Player player, final ValidDestination validDestination, final MessageId messageId)
	{
		teleportExecutor.execute(player, validDestination, messageId);
	}


	/**
	 * Get bedspawn destination for a player
	 *
	 * @param player the player
	 * @return the player bedspawn destination wrapped in an {@link Optional}
	 */
	final Destination getHomeDestination(final Player player)
	{
		// if player is null, return empty optional
		if (player == null)
		{
			return new InvalidDestination("player home", "Player was null.");
		}

		// get player respawn (bed or other) location
		Location location = player.getRespawnLocation();

		// Get home display name
		String destinationName = plugin.messageBuilder.getHomeDisplayName().orElse("Home");

		// return destination for player bed spawn location
		return Destination.of(destinationName, location, DestinationType.HOME);
	}


	/**
	 * Get spawn destination for a player
	 *
	 * @param player the player
	 * @return the player spawn destination wrapped in an {@link Optional}
	 */
	final Destination getSpawnDestination(final Player player)
	{
		// if player is null, return empty optional
		if (player == null)
		{
			return new InvalidDestination("world spawn", "Player was null.");
		}

		// get spawn location for player
		Location location = plugin.worldManager.getSpawnLocation(player.getWorld());

		// if from-nether is enabled in config and player is in nether, try to get overworld spawn location
		if (isInNetherWorld(player) && plugin.getConfig().getBoolean("from-nether"))
		{
			location = getOverworldSpawnLocation(player).orElse(location);
		}
		// if from-end is enabled in config and player is in end, try to get overworld spawn location
		else if (isInEndWorld(player) && plugin.getConfig().getBoolean("from-end"))
		{
			location = getOverworldSpawnLocation(player).orElse(location);
		}

		// get destination name
		String destinationName = plugin.messageBuilder.getSpawnDisplayName().orElse("Spawn");

		// return destination for player spawn
		return Destination.of(destinationName, location, DestinationType.SPAWN);
	}


	/**
	 * Get overworld spawn location corresponding to a player nether or end world.
	 *
	 * @param player the passed player whose current world will be used to find a matching over world spawn location
	 * @return {@link Optional} wrapped spawn location of the normal world associated with the passed player
	 * nether or end world, or the current player world spawn location if no matching normal world found
	 */
	private Optional<Location> getOverworldSpawnLocation(final Player player)
	{
		// check for null parameter
		if (player == null)
		{
			return Optional.empty();
		}

		// create list to store normal environment worlds
		List<World> normalWorlds = new ArrayList<>();

		// iterate through all server worlds
		for (World checkWorld : plugin.getServer().getWorlds())
		{

			// if world is normal environment, try to match name to passed world
			if (checkWorld.getEnvironment().equals(World.Environment.NORMAL))
			{

				// check if normal world matches passed world minus nether/end suffix
				if (checkWorld.getName().equals(player.getWorld().getName().replaceFirst("(_nether$|_the_end$)", "")))
				{
					return Optional.of(plugin.worldManager.getSpawnLocation(checkWorld));
				}

				// if no match, add to list of normal worlds
				normalWorlds.add(checkWorld);
			}
		}

		// if only one normal world exists, return that world
		if (normalWorlds.size() == 1)
		{
			return Optional.of(plugin.worldManager.getSpawnLocation(normalWorlds.getFirst()));
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
	private boolean isInNetherWorld(final Player player)
	{
		return player.getWorld().getEnvironment().equals(World.Environment.NETHER);
	}


	/**
	 * Check if a player is in an end world
	 *
	 * @param player the player
	 * @return true if player is in an end world, false if not
	 */
	private boolean isInEndWorld(final Player player)
	{
		return player.getWorld().getEnvironment().equals(World.Environment.THE_END);
	}


	/**
	 * Send invalid destination message to player
	 *
	 * @param player          the player
	 * @param destinationName the destination name
	 */
	final void sendInvalidDestinationMessage(final Player player, final String destinationName)
	{
		plugin.messageBuilder.compose(player, MessageId.TELEPORT_FAIL_INVALID_DESTINATION)
				.setMacro(Macro.DESTINATION, destinationName)
				.send();
		plugin.soundConfig.playSound(player, SoundId.TELEPORT_CANCELLED);
	}

}
