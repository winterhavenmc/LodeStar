package com.winterhaven_mc.lodestar.util;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.storage.Destination;
import com.winterhaven_mc.util.ConfigAccessor;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Implements message manager for <code>LodeStar</code>.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class MessageManager {

	private final PluginMain plugin; // reference to main class
	private ConfigAccessor messages;
	private ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> messageCooldownMap;
	private String language;

	/**
	 * Constructor method for class
	 * 
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// create pointer to main class
		this.plugin = plugin;

		// install localization files
		this.installLocalizationFiles();

		// get configured language
		this.language = languageFileExists(plugin.getConfig().getString("language"));

		// instantiate custom configuration manager for language file
		this.messages = new ConfigAccessor(plugin, "language" + File.separator + this.language + ".yml");

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<UUID,ConcurrentHashMap<String,Long>>();
	}


	/**
	 *  Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 */
	public void sendPlayerMessage(final CommandSender sender, final String messageId) {
		this.sendPlayerMessage(sender, messageId, 1, "", "");
	}

	/**
	 * Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			number of items
	 */
	public void sendPlayerMessage(final CommandSender sender, final String messageId, final Integer quantity) {
		this.sendPlayerMessage(sender, messageId, quantity, "", "");
	}

	/**
	 * Send message to player
	 * 
	 * @param sender			player recieving message
	 * @param messageId			message identifier in messages file
	 * @param destinationName	name of destination
	 */
	public void sendPlayerMessage(final CommandSender sender, final String messageId, final String destinationName) {
		this.sendPlayerMessage(sender, messageId, 1, destinationName,"");
	}

	/**
	 * Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			number of items
	 * @param destinationName	name of destination
	 */
	public void sendPlayerMessage(final CommandSender sender, final String messageId,
								  final Integer quantity, final String destinationName) {
		this.sendPlayerMessage(sender, messageId, quantity, destinationName,"");
	}

	/** Send message to player
	 * 
	 * @param sender					Player receiving message
	 * @param messageId					message identifier in messages file
	 * @param quantity					number of items
	 * @param passedDestinationName		name of destination
	 * @param passedTargetPlayerName	name of player targeted
	 */
	public void sendPlayerMessage(final CommandSender sender,
								  final String messageId,
								  final Integer quantity,
								  final String passedDestinationName,
								  final String passedTargetPlayerName) {

		// if message is not enabled in messages file, do nothing and return
		if (!messages.getConfig().getBoolean("messages." + messageId + ".enabled")) {
			return;
		}

		// set substitution variable defaults			
		String playerName = "console";
		String playerNickname = "console";
		String playerDisplayName = "console";
		String worldName = "world";
		String targetPlayerName = "player";
		String destinationName = "unknown";
		String cooldownString = "";
		String warmupString;

		if (passedTargetPlayerName != null && !passedTargetPlayerName.isEmpty()) {
			targetPlayerName = passedTargetPlayerName;
		}

		if (passedDestinationName != null && !passedDestinationName.isEmpty()) {
			destinationName = passedDestinationName;
		}

		// if sender is a player...
		if (sender instanceof Player) {

			Player player = (Player) sender;

			// get message cooldown time remaining
			Long lastDisplayed = getMessageCooldown(player,messageId);

			// get message repeat delay
			int messageRepeatDelay = messages.getConfig().getInt("messages." + messageId + ".repeat-delay");

			// if message has repeat delay value and was displayed to player more recently, do nothing and return
			if (lastDisplayed > System.currentTimeMillis() - messageRepeatDelay * 1000) {
				return;
			}

			// if repeat delay value is greater than zero, add entry to messageCooldownMap
			if (messageRepeatDelay > 0) {
				putMessageCooldown(player,messageId);
			}

			// assign player dependent variables
			playerName = player.getName();
			playerNickname = player.getPlayerListName();
			playerDisplayName = player.getDisplayName();
			worldName = player.getWorld().getName();
			cooldownString = getTimeString(plugin.teleportManager.getCooldownTimeRemaining(player));
		}

		// get message from file
		String message = messages.getConfig().getString("messages." + messageId + ".string");

		// get item name and strip color codes
		String itemName = getItemName();

		// get warmup value from config file
		warmupString = getTimeString(plugin.getConfig().getLong("teleport-warmup"));

		// if destination is spawn...
		String key = Destination.deriveKey(destinationName);

		if (key.equals(Destination.deriveKey("spawn"))
				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName())) ) {

			String overworldname = worldName.replaceFirst("(_nether|_the_end)$", "");

			// if from-nether is enabled in config and player is in nether,
			// get overworld name
			if (plugin.getConfig().getBoolean("from-nether")
					&& worldName.endsWith("_nether")
					&& plugin.getServer().getWorld(overworldname) != null) {
				worldName = overworldname;
			}

			// if from-end is enabled in config, and player is in end, get
			// overworld name
			if (plugin.getConfig().getBoolean("from-end")
					&& worldName.endsWith("_the_end")
					&& plugin.getServer().getWorld(overworldname) != null) {
				worldName = overworldname;
			}

			// set destination string to spawn display name from messages file
			destinationName = getSpawnDisplayName();
		}

		// if destination is home, get home display name from messages file
		if (key.equals(Destination.deriveKey("home"))
				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {

			// set destination string to home display name from messages file
			destinationName = getHomeDisplayName();
		}

		// get world name from WorldManager
		worldName = plugin.worldManager.getWorldName(worldName);

		// if quantity is greater than one, use plural item name
		if (quantity > 1) {
			// get plural item name
			itemName = getItemNamePlural();
		}

		// replace underscores with spaces in destination name
		destinationName = destinationName.replace('_', ' ');

		// do variable substitutions
		if (message.contains("%")) {
			message = StringUtil.replace(message,"%itemname%", itemName);
			message = StringUtil.replace(message,"%playername%", playerName);
			message = StringUtil.replace(message,"%playerdisplayname%", playerDisplayName);
			message = StringUtil.replace(message,"%playernickname%", playerNickname);
			message = StringUtil.replace(message,"%worldname%", worldName);
			message = StringUtil.replace(message,"%timeremaining%", cooldownString);
			message = StringUtil.replace(message,"%warmuptime%", warmupString);
			message = StringUtil.replace(message,"%quantity%", quantity.toString());
			message = StringUtil.replace(message,"%destination%", destinationName);
			message = StringUtil.replace(message,"%targetplayer%", targetPlayerName);

			// do variable substitutions, stripping color codes from all caps variables
			message = StringUtil.replace(message,"%ITEMNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',itemName)));
			message = StringUtil.replace(message,"%PLAYERNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerName)));
			message = StringUtil.replace(message,"%PLAYERNICKNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerNickname)));
			message = StringUtil.replace(message,"%WORLDNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',worldName)));
			message = StringUtil.replace(message,"%DESTINATION%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',destinationName)));
			message = StringUtil.replace(message,"%TARGETPLAYER%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',targetPlayerName)));

			// no stripping of color codes necessary, but do variable substitutions anyhow
			// in case all caps variables were used
			message = StringUtil.replace(message,"%PLAYERDISPLAYNAME%", playerDisplayName);
			message = StringUtil.replace(message,"%TIMEREMAINING%", cooldownString);
			message = StringUtil.replace(message,"%WARMUPTIME%", warmupString);
			message = StringUtil.replace(message,"%QUANTITY%", quantity.toString());
		}

		// send message to player
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
	}


	/**
	 * Play sound effect for action
	 * @param sender the player for whom to play a sound
	 * @param soundId the sound identifier
	 */
	public void playerSound(final CommandSender sender, final String soundId) {

		if (sender instanceof Player) {
			playerSound((Player)sender,soundId);
		}
	}


	/**
	 * Play sound effect for action
	 * @param player the player for whom to play a sound
	 * @param soundId the sound identifier
	 */
	public void playerSound(final Player player, final String soundId) {

		// if sound effects are disabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("sound-effects")) {
			return;
		}

		// if sound is set to enabled in config file
		if (plugin.getConfig().getBoolean("sounds." + soundId + ".enabled")) {

			// get player only setting from config file
			boolean playerOnly = plugin.getConfig().getBoolean("sounds." + soundId + ".player-only");

			// get sound name from config file
			String soundName = plugin.getConfig().getString("sounds." + soundId + ".sound");

			// get sound volume from config file
			float volume = (float) plugin.getConfig().getDouble("sounds." + soundId + ".volume");

			// get sound pitch from config file
			float pitch = (float) plugin.getConfig().getDouble("sounds." + soundId + ".pitch");

			try {
				// if sound is set player only, use player.playSound()
				if (playerOnly) {
					player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
				}
				// else use world.playSound() so other players in vicinity can hear
				else {
					player.getWorld().playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
				}
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("An error occured while trying to play the sound '" + soundName 
						+ "'. You probably need to update the sound name in your config.yml file.");
			}
		}
	}


	/**
	 * Add entry to message cooldown map
	 * @param player the player to add to the message cooldown map
	 * @param messageId the message identifier to be added to the cooldown map
	 */
	private void putMessageCooldown(final Player player, final String messageId) {

		ConcurrentHashMap<String, Long> tempMap = new ConcurrentHashMap<String, Long>();
		tempMap.put(messageId, System.currentTimeMillis());
		messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player the player whose uuid to use as key to retrieve cooldown time from map
	 * @param messageId the message identifier to use as key to retrieve cooldown time from map
	 * @return cooldown expire time
	 */
	private long getMessageCooldown(final Player player, final String messageId) {

		// check if player is in message cooldown hashmap
		if (messageCooldownMap.containsKey(player.getUniqueId())) {

			// check if messageID is in player's cooldown hashmap
			if (messageCooldownMap.get(player.getUniqueId()).containsKey(messageId)) {

				// return cooldown time
				return messageCooldownMap.get(player.getUniqueId()).get(messageId);
			}
		}
		return 0L;
	}


	/**
	 * Remove player from message cooldown map
	 * @param player the player whose uuid will be removed from the cooldown map
	 */
	public void removePlayerCooldown(final Player player) {
		messageCooldownMap.remove(player.getUniqueId());
	}


	public void reload() {

		// reinstall message files if necessary
		installLocalizationFiles();

		// get currently configured language
		String newLanguage = languageFileExists(plugin.getConfig().getString("language"));

		// if configured language has changed, instantiate new messages object
		if (!newLanguage.equals(this.language)) {
			this.messages = new ConfigAccessor(plugin, "language" + File.separator + newLanguage + ".yml");
			this.language = newLanguage;
			plugin.getLogger().info("New language " + this.language + " enabled.");
		}

		// reload language file
		messages.reloadConfig();
	}


	/**
	 * Install localization files from <em>language</em> directory in jar 
	 */
	private void installLocalizationFiles() {

		List<String> filelist = new ArrayList<String>();

		// get the absolute path to this plugin as URL
		URL pluginURL = plugin.getServer().getPluginManager().getPlugin(plugin.getName()).getClass().getProtectionDomain().getCodeSource().getLocation();

		// read files contained in jar, adding language/*.yml files to list
		ZipInputStream zip;
		try {
			zip = new ZipInputStream(pluginURL.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null) {
					break;
				}
				String name = e.getName();
				if (name.startsWith("language" + '/') && name.endsWith(".yml")) {
					filelist.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}

		// iterate over list of language files and install from jar if not already present
		for (String filename : filelist) {
			// this check prevents a warning message when files are already installed
			if (new File(plugin.getDataFolder() + File.separator + filename).exists()) {
				continue;
			}
			plugin.saveResource(filename, false);
			plugin.getLogger().info("Installed localization file:  " + filename);
		}
	}


	private String languageFileExists(final String language) {

		// check if localization file for configured language exists, if not then fallback to en-US
		File languageFile = new File(plugin.getDataFolder() 
				+ File.separator + "language" 
				+ File.separator + language + ".yml");

		if (languageFile.exists()) {
			return language;
		}
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");
		return "en-US";
	}


	public String getLanguage() {
		return this.language;
	}


	public String getItemName() {
		return messages.getConfig().getString("item-name");
	}


	@SuppressWarnings("WeakerAccess")
	public String getItemNamePlural() {
		return messages.getConfig().getString("item-name-plural");
	}


	public String getInventoryItemName() {
		return messages.getConfig().getString("inventory-item-name");
	}


	public String getSpawnDisplayName() {
		return messages.getConfig().getString("spawn-display-name");
	}


	public String getHomeDisplayName() {
		return messages.getConfig().getString("home-display-name");
	}


	public List<String> getItemLore() {
		return messages.getConfig().getStringList("item-lore");
	}


	/**
	 * Format the time string with hours, minutes, seconds
	 * @return the formatted time string
	 */
	private String getTimeString(long duration) {

		StringBuilder timeString = new StringBuilder();

		int hours =   (int)duration / 3600;
		int minutes = (int)(duration % 3600) / 60;
		int seconds = (int)duration % 60;

		String hour_string = this.messages.getConfig().getString("hour");
		String hour_plural_string = this.messages.getConfig().getString("hour_plural");
		String minute_string = this.messages.getConfig().getString("minute");
		String minute_plural_string = this.messages.getConfig().getString("minute_plural");
		String second_string = this.messages.getConfig().getString("second");
		String second_plural_string = this.messages.getConfig().getString("second_plural");

		if (hours > 1) {
			timeString.append(hours);
			timeString.append(' ');
			timeString.append(hour_plural_string);
			timeString.append(' ');
		}
		else if (hours == 1) {
			timeString.append(hours);
			timeString.append(' ');
			timeString.append(hour_string);
			timeString.append(' ');
		}

		if (minutes > 1) {
			timeString.append(minutes);
			timeString.append(' ');
			timeString.append(minute_plural_string);
			timeString.append(' ');
		}
		else if (minutes == 1) {
			timeString.append(minutes);
			timeString.append(' ');
			timeString.append(minute_string);
			timeString.append(' ');
		}

		if (seconds > 1) {
			timeString.append(seconds);
			timeString.append(' ');
			timeString.append(second_plural_string);
		}
		else if (seconds == 1) {
			timeString.append(seconds);
			timeString.append(' ');
			timeString.append(second_string);
		}

		return timeString.toString().trim();
	}

}
