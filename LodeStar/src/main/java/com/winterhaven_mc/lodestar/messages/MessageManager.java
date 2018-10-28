package com.winterhaven_mc.lodestar.messages;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.storage.Destination;

import com.winterhaven_mc.util.YamlLanguageManager;
import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.StringUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implements message manager for LodeStar
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class MessageManager {

	// reference to main class
	private final PluginMain plugin;

	// hashmap for per player message cooldown
	private final ConcurrentHashMap<UUID, EnumMap<MessageId, Long>> messageCooldownMap;

	// language manager
	private final LanguageManager languageManager;

	// configuration object for messages
	private Configuration messages;


	/**
	 * Constructor method for class
	 * 
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<>();

		// instantiate messageFileHelper
		this.languageManager = new YamlLanguageManager(plugin);

		// load messages from file
		this.messages = languageManager.loadMessages();
	}


	/**
	 *  Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 */
	public void sendPlayerMessage(final CommandSender sender, final MessageId messageId) {
		this.sendPlayerMessage(sender, messageId, 1, "", "");
	}

	/**
	 * Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			number of items
	 */
	public void sendPlayerMessage(final CommandSender sender, final MessageId messageId, final Integer quantity) {
		this.sendPlayerMessage(sender, messageId, quantity, "", "");
	}

	/**
	 * Send message to player
	 * 
	 * @param sender			player recieving message
	 * @param messageId			message identifier in messages file
	 * @param destinationName	name of destination
	 */
	public void sendPlayerMessage(final CommandSender sender, final MessageId messageId, final String destinationName) {
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
	public void sendPlayerMessage(final CommandSender sender, final MessageId messageId,
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
								  final MessageId messageId,
								  final Integer quantity,
								  final String passedDestinationName,
								  final String passedTargetPlayerName) {

		// if message is not enabled in messages file, do nothing and return
		if (!isEnabled(messageId)) {
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
			long lastDisplayed = getMessageCooldown(player,messageId);

			// get message repeat delay
			int messageRepeatDelay = getRepeatDelay(messageId);

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
		String message = getMessage(messageId);

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
	 * Add entry to message cooldown map
	 * @param player the player to insert in the message cooldown map
	 * @param messageId the message identifier to insert in the cooldown map
	 */
	private void putMessageCooldown(final Player player, final MessageId messageId) {

		final EnumMap<MessageId, Long> tempMap = new EnumMap<>(MessageId.class);
		tempMap.put(messageId, System.currentTimeMillis());
		this.messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player the player for whom to retrieve cooldown time
	 * @param messageId the message identifier for which retrieve cooldown time
	 * @return cooldown expire time
	 */
	private long getMessageCooldown(final Player player, final MessageId messageId) {

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
	 * @param player the player to be removed from the message cooldown map
	 */
	public final void removePlayerCooldown(final Player player) {
		messageCooldownMap.remove(player.getUniqueId());
	}


	/**
	 * Reload messages
	 */
	public final void reload() {

		// reload messages
		this.messages = languageManager.loadMessages();
	}


	/**
	 * Check if message is enabled
	 * @param messageId message identifier to check
	 * @return true if message is enabled, false if not
	 */
	private boolean isEnabled(MessageId messageId) {
		return !messages.getBoolean("messages." + messageId.toString() + ".enabled");
	}


	/**
	 * get message repeat delay from language file
	 * @param messageId message identifier to retrieve message delay
	 * @return int message repeat delay in seconds
	 */
	private int getRepeatDelay(MessageId messageId) {
		return messages.getInt("messages." + messageId.toString() + ".repeat-delay");
	}


	/**
	 * get message text from language file
	 * @param messageId message identifier to retrieve message text
	 * @return String message text
	 */
	private String getMessage(MessageId messageId) {
		return messages.getString("messages." + messageId.toString() + ".string");
	}


	/**
	 * Get item name from language file
	 * @return the formatted display name of the SpawnStar item
	 */
	public final String getItemName() {
		return messages.getString("item-name");
	}


	/**
	 * Get configured plural item name from language file
	 * @return the formatted plural display name of the SpawnStar item
	 */
	@SuppressWarnings("WeakerAccess")
	public final String getItemNamePlural() {
		return messages.getString("item-name-plural");
	}


	public String getInventoryItemName() {
		return messages.getString("inventory-item-name");
	}


	public String getSpawnDisplayName() {
		return messages.getString("spawn-display-name");
	}


	public String getHomeDisplayName() {
		return messages.getString("home-display-name");
	}


	public List<String> getItemLore() {
		return messages.getStringList("item-lore");
	}


	/**
	 * Format the time string with hours, minutes, seconds
	 * @param duration the time duration (in seconds) to convert to string
	 * @return the formatted time string
	 */
	public String getTimeString(long duration) {

		StringBuilder timeString = new StringBuilder();

		int hours =   (int)duration / 3600;
		int minutes = (int)(duration % 3600) / 60;
		int seconds = (int)duration % 60;

		String hour_string = this.messages.getString("hour");
		String hour_plural_string = this.messages.getString("hour_plural");
		String minute_string = this.messages.getString("minute");
		String minute_plural_string = this.messages.getString("minute_plural");
		String second_string = this.messages.getString("second");
		String second_plural_string = this.messages.getString("second_plural");

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
