package com.winterhaven_mc.lodestar.messages;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.util.AbstractMessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implements message manager for LodeStar
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class MessageManager extends AbstractMessageManager {

	// reference to main class
	private final PluginMain plugin;


	/**
	 * Constructor method for class
	 * 
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// call super class constructor
		//noinspection unchecked
		super(plugin, MessageId.class);
		this.plugin = plugin;
	}


	@Override
	protected Map<String,String> getDefaultReplacements(CommandSender recipient) {

		Map<String,String> replacements = new HashMap<>();

		// strip color codes
		replacements.put("%PLAYER_NAME%",ChatColor.stripColor(recipient.getName()));
		replacements.put("%WORLD_NAME%",ChatColor.stripColor(getWorldName(recipient)));
		replacements.put("%ITEM_NAME%", ChatColor.stripColor(getItemName()));
		replacements.put("%QUANTITY%","1");
		replacements.put("%MATERIAL%","unknown");
		replacements.put("%DESTINATION_NAME%",ChatColor.stripColor(getSpawnDisplayName()));
		replacements.put("%TARGET_PLAYER%","target player");
		replacements.put("%WARMUP_TIME%",getTimeString(plugin.getConfig().getInt("teleport-warmup")));

		// leave color codes intact
		replacements.put("%player_name%",recipient.getName());
		replacements.put("%world_name%",getWorldName(recipient));
		replacements.put("%item_name%",getItemName());
		replacements.put("%destination_name%",getSpawnDisplayName());
		replacements.put("%target_player%","target player");

		// if recipient is player, get remaining cooldown time from teleport manager
		if (recipient instanceof Player) {
			replacements.put("%COOLDOWN_TIME%",
					getTimeString(plugin.teleportManager.getCooldownTimeRemaining((Player)recipient)));
		}
		else {
			replacements.put("%COOLDOWN_TIME",getTimeString(0L));
		}

		return replacements;
	}


	/**
	 *  Send message to player
	 * 
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 */
	public void sendMessage(final CommandSender recipient, final MessageId messageId) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}

	/**
	 * Send message to player
	 * 
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			number of items
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final Integer quantity) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// set passed quantity in replacements map
		replacements.put("%QUANTITY%",quantity.toString());

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}

	/**
	 * Send message to player
	 * 
	 * @param recipient			player recieving message
	 * @param messageId			message identifier in messages file
	 * @param destinationName	name of destination
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final String destinationName) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// set destination name in replacement map
		replacements.put("%DESTINATION_NAME%",ChatColor.stripColor(destinationName));
		replacements.put("%destination_name%",destinationName);

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}

	/**
	 * Send message to player
	 * 
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			number of items
	 * @param destinationName	name of destination
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final Integer quantity,
							final String destinationName) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// set quantity in replacement map
		replacements.put("%QUANTITY%",quantity.toString());

		// set destination name in replacement map
		replacements.put("%DESTINATION_NAME%",ChatColor.stripColor(destinationName));
		replacements.put("%destination_name%",destinationName);

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}

	/** Send message to player
	 * 
	 * @param recipient				Player receiving message
	 * @param messageId				message identifier in messages file
	 * @param quantity				number of items
	 * @param destinationName		name of destination
	 * @param targetPlayerName		name of player targeted
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final Integer quantity,
							final String destinationName,
							final String targetPlayerName) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// set quantity in replacement map
		replacements.put("%QUANTITY%",quantity.toString());

		// set destination name in replacement map
		replacements.put("%DESTINATION_NAME%",ChatColor.stripColor(destinationName));
		replacements.put("%destination_name%",destinationName);

		// set target player name in replacement map
		replacements.put("%TARGET_PLAYER%",ChatColor.stripColor(targetPlayerName));
		replacements.put("%target_player%",targetPlayerName);

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}

//		// if message is not enabled in messages file, do nothing and return
//		if (!isEnabled(messageId)) {
//			return;
//		}
//
//		// set substitution variable defaults
//		String playerName = "console";
//		String playerNickname = "console";
//		String playerDisplayName = "console";
//		String worldName = "world";
//		String targetPlayerName = "player";
//		String destinationName = "unknown";
//		String cooldownString = "";
//		String warmupString;
//
//		if (passedTargetPlayerName != null && !passedTargetPlayerName.isEmpty()) {
//			targetPlayerName = passedTargetPlayerName;
//		}
//
//		if (passedDestinationName != null && !passedDestinationName.isEmpty()) {
//			destinationName = passedDestinationName;
//		}
//
//		// if recipient is a player...
//		if (recipient instanceof Player) {
//
//			Player player = (Player) recipient;
//
//			// get message cooldown time remaining
//			long lastDisplayed = getMessageCooldown(player,messageId);
//
//			// get message repeat delay
//			int messageRepeatDelay = getRepeatDelay(messageId);
//
//			// if message has repeat delay value and was displayed to player more recently, do nothing and return
//			if (lastDisplayed > System.currentTimeMillis() - messageRepeatDelay * 1000) {
//				return;
//			}
//
//			// if repeat delay value is greater than zero, add entry to messageCooldownMap
//			if (messageRepeatDelay > 0) {
//				putMessageCooldown(player,messageId);
//			}
//
//			// assign player dependent variables
//			playerName = player.getName();
//			playerNickname = player.getPlayerListName();
//			playerDisplayName = player.getDisplayName();
//			worldName = player.getWorld().getName();
//			cooldownString = getTimeString(plugin.teleportManager.getCooldownTimeRemaining(player));
//		}
//
//		// get message from file
//		String message = getMessage(messageId);
//
//		// get item name and strip color codes
//		String itemName = getItemName();
//
//		// get warmup value from config file
//		warmupString = getTimeString(plugin.getConfig().getLong("teleport-warmup"));
//
//		// if destination is spawn...
//		String key = Destination.deriveKey(destinationName);
//
//		if (key.equals(Destination.deriveKey("spawn"))
//				|| key.equals(Destination.deriveKey(plugin.messageManager.getSpawnDisplayName())) ) {
//
//			String overworldname = worldName.replaceFirst("(_nether|_the_end)$", "");
//
//			// if from-nether is enabled in config and player is in nether,
//			// get overworld name
//			if (plugin.getConfig().getBoolean("from-nether")
//					&& worldName.endsWith("_nether")
//					&& plugin.getServer().getWorld(overworldname) != null) {
//				worldName = overworldname;
//			}
//
//			// if from-end is enabled in config, and player is in end, get
//			// overworld name
//			if (plugin.getConfig().getBoolean("from-end")
//					&& worldName.endsWith("_the_end")
//					&& plugin.getServer().getWorld(overworldname) != null) {
//				worldName = overworldname;
//			}
//
//			// set destination string to spawn display name from messages file
//			destinationName = getSpawnDisplayName();
//		}
//
//		// if destination is home, get home display name from messages file
//		if (key.equals(Destination.deriveKey("home"))
//				|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
//
//			// set destination string to home display name from messages file
//			destinationName = getHomeDisplayName();
//		}
//
//		// get world name from WorldManager
//		worldName = plugin.worldManager.getWorldName(worldName);
//
//		// if quantity is greater than one, use plural item name
//		if (quantity > 1) {
//			// get plural item name
//			itemName = getItemNamePlural();
//		}
//
//		// replace underscores with spaces in destination name
//		destinationName = destinationName.replace('_', ' ');
//
//		// do variable substitutions
//		if (message.contains("%")) {
//			message = StringUtil.replace(message,"%itemname%", itemName);
//			message = StringUtil.replace(message,"%playername%", playerName);
//			message = StringUtil.replace(message,"%playerdisplayname%", playerDisplayName);
//			message = StringUtil.replace(message,"%playernickname%", playerNickname);
//			message = StringUtil.replace(message,"%worldname%", worldName);
//			message = StringUtil.replace(message,"%timeremaining%", cooldownString);
//			message = StringUtil.replace(message,"%warmuptime%", warmupString);
//			message = StringUtil.replace(message,"%quantity%", quantity.toString());
//			message = StringUtil.replace(message,"%destination%", destinationName);
//			message = StringUtil.replace(message,"%targetplayer%", targetPlayerName);
//
//			// do variable substitutions, stripping color codes from all caps variables
//			message = StringUtil.replace(message,"%ITEMNAME%",
//					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',itemName)));
//			message = StringUtil.replace(message,"%PLAYERNAME%",
//					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerName)));
//			message = StringUtil.replace(message,"%PLAYERNICKNAME%",
//					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerNickname)));
//			message = StringUtil.replace(message,"%WORLDNAME%",
//					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',worldName)));
//			message = StringUtil.replace(message,"%DESTINATION%",
//					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',destinationName)));
//			message = StringUtil.replace(message,"%TARGETPLAYER%",
//					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',targetPlayerName)));
//
//			// no stripping of color codes necessary, but do variable substitutions anyhow
//			// in case all caps variables were used
//			message = StringUtil.replace(message,"%PLAYERDISPLAYNAME%", playerDisplayName);
//			message = StringUtil.replace(message,"%TIMEREMAINING%", cooldownString);
//			message = StringUtil.replace(message,"%WARMUPTIME%", warmupString);
//			message = StringUtil.replace(message,"%QUANTITY%", quantity.toString());
//		}
//
//		// send message to player
//		recipient.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
//	}


	/**
	 * Get configured plural item name from language file
	 * @return the formatted plural display name of the SpawnStar item
	 */
	@SuppressWarnings({"WeakerAccess","unused"})
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
