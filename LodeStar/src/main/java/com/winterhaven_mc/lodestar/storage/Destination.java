package com.winterhaven_mc.lodestar.storage;

import com.winterhaven_mc.lodestar.PluginMain;

import com.winterhaven_mc.util.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;


public class Destination {

	private static final PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private static final LanguageManager languageManager = LanguageManager.getInstance();

	private final String key;
	private final String displayName;
	private final Location location;


	/**
	 * Class constructor
	 *
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(final String displayName, final Location location) {

		this.key = deriveKey(displayName);
		this.displayName = displayName;
		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
	}


	/**
	 * Class constructor
	 *
	 * @param key         the destination key value
	 * @param displayName the destination display name string
	 * @param location    the destination location
	 */
	public Destination(final String key, final String displayName, final Location location) {

		this.key = key;
		this.displayName = displayName;
		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
	}


	@Override
	public String toString() {
		return getDisplayName();
	}


	/**
	 * Check if destination is spawn location
	 *
	 * @return true if spawn, else false
	 */
	public boolean isSpawn() {
		return this.getKey().equals("spawn")
				|| this.getKey().equals(deriveKey(languageManager.getSpawnDisplayName()));
	}


	/**
	 * Check if destination is home location
	 *
	 * @return true if home, else false
	 */
	@SuppressWarnings("unused")
	public boolean isHome() {
		return this.getKey().equals("home")
				|| this.getKey().equals(deriveKey(languageManager.getHomeDisplayName()));
	}


	/**
	 * Getter for destination key field
	 *
	 * @return the value of the key field
	 */
	String getKey() {
		return key;
	}


	/**
	 * Getter for destination displayName field
	 *
	 * @return the value of the displayName field
	 */
	public String getDisplayName() {
		return displayName;
	}


	/**
	 * Getter for destination location field
	 *
	 * @return the value of the location field
	 */
	public Location getLocation() {
		return location;
	}


	/* STATIC METHODS */

	/**
	 * Derive key from destination display name<br>
	 * replaces spaces with underscores, strips color codes and folds to lower case
	 *
	 * @param destinationName the destination name string to convert to a key value
	 * @return the key value derived from the destination name
	 */
	public static String deriveKey(final String destinationName) {

		// copy passed in destination name to derivedKey
		String derivedKey = destinationName;

		// replace spaces with underscores
		derivedKey = derivedKey.replace(' ', '_');

		// convert to lowercase
		derivedKey = derivedKey.toLowerCase();

		// translate alternate color codes
		derivedKey = ChatColor.translateAlternateColorCodes('&', derivedKey);

		// strip all color codes
		derivedKey = ChatColor.stripColor(derivedKey);

		return derivedKey;
	}


	/**
	 * Check if destination exists in storage or is reserved name
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination exists, {@code false} if it does not
	 */
	public static boolean exists(final String destinationName) {

		if (destinationName == null || destinationName.isEmpty()) {
			return false;
		}

		String key = Destination.deriveKey(destinationName);

		return isReserved(destinationName) || plugin.dataStore.getRecord(key) != null;
	}


	/**
	 * Check if destination name is a reserved name
	 *
	 * @param destinationName the destination name to test for reserved name
	 * @return {@code true} if destination is a reserved name, {@code false} if it is not
	 */
	public static boolean isReserved(final String destinationName) {

		// test if passed destination name is reserved name for home or spawn locations
		return isHome(destinationName) || isSpawn(destinationName);
	}


	/**
	 * Check if passed destination name is reserved name for home location
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination name is reserved home name, {@code false} if not
	 */
	public static boolean isHome(final String destinationName) {

		// derive key from destination name to normalize string (strip colors, fold to lowercase, etc)
		String key = Destination.deriveKey(destinationName);
		return key.equals("home")
				|| key.equals(Destination.deriveKey(languageManager.getHomeDisplayName()));
	}


	/**
	 * Check if passed destination name is reserved name for spawn location
	 *
	 * @param destinationName the destination name to check
	 * @return {@code true} if destination name is reserved spawn name, {@code false} if not
	 */
	public static boolean isSpawn(final String destinationName) {

		// derive key from destination name to normalize string (strip colors, fold to lowercase, etc)
		String key = Destination.deriveKey(destinationName);
		return key.equals("spawn")
				|| key.equals(Destination.deriveKey(languageManager.getSpawnDisplayName()));
	}


	/**
	 * Get destination display name from destination represented by passed key
	 *
	 * @param key the key of the destination for which to retrieve display name
	 * @return String - the formatted display name for the destination, or the key if no display name found
	 */
	public static String getName(final String key) {

		String returnName = key;

		// get derived key in case destination name was passed; this will have no effect on a key
		String derivedKey = Destination.deriveKey(key);

		// if destination is spawn get spawn display name from messages files
		if (derivedKey.equals("spawn")
				|| derivedKey.equals(Destination.deriveKey(languageManager.getSpawnDisplayName()))) {
			returnName = languageManager.getSpawnDisplayName();
		}

		// if destination is home get home display name from messages file
		else if (derivedKey.equals("home")
				|| derivedKey.equals(Destination.deriveKey(languageManager.getHomeDisplayName()))) {
			returnName = languageManager.getHomeDisplayName();
		}

		// else get destination name from datastore
		else {
			Destination destination = plugin.dataStore.getRecord(derivedKey);
			if (destination != null) {
				returnName = destination.getDisplayName();
			}
		}

		// if no destination name found, use derived key for name
		if (returnName == null) {
			returnName = derivedKey;
		}

		return returnName;
	}

}
