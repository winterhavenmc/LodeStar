/**
 * Provides types representing abstract in-game destinations.
 * <p>
 * The {@link com.winterhavenmc.lodestar.models.destination.Destination} sealed interface represents a general abstraction of a target location
 * within the Minecraft world. It unifies concepts such as homes, spawns, stored waypoints, and invalid destinations
 * for use cases including teleportation, message display, and persistent storage.
 * </p>
 *
 * <p>
 * There are two permitted types of {@code Destination}:
 * </p>
 * <ul>
 *     <li>{@link com.winterhavenmc.lodestar.models.destination.ValidDestination} – Represents a fully resolved and usable destination, consisting of
 *         a {@code displayName} and a {@code location}. The {@code key()} method provides a derived
 *         identifier based on the display name.</li>
 *     <li>{@link com.winterhavenmc.lodestar.models.destination.InvalidDestination} – Represents a destination that could not be resolved, including
 *         a display name and a human-readable reason for invalidity.</li>
 * </ul>
 *
 * <p>
 * Permitted implementations of {@code ValidDestination} are:
 * <ul>
 *     <li>{@code HomeDestination} – A player's bedspawn or other respawn location.</li>
 *     <li>{@code SpawnDestination} – A world or server spawn point.</li>
 *     <li>{@code StoredDestination} – A destination persisted in a resource or datastore.</li>
 * </ul>
 * </p>
 *
 * <p>
 * All destination types are implemented as immutable Java {@code record} classes and designed to
 * support identity, serialization, and display formatting. They may be used in commands, data persistence,
 * and as arguments to systems requiring a {@link org.bukkit.Location}.
 * </p>
 *
 * @author Tim Savage
 * @since 1.0
 */
package com.winterhavenmc.lodestar.models.destination;
