package com.winterhaven_mc.lodestar.messages;


import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.util.AbstractMessageManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Implements message manager for LodeStar
 *
 * @author Tim Savage
 * @version 1.0
 */
public class MessageManager extends AbstractMessageManager<MessageId> {

	// reference to main class
	private final PluginMain plugin;


	/**
	 * Constructor method for class
	 *
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// call super class constructor
		super(plugin, MessageId.class);

		// set reference to main class
		this.plugin = plugin;
	}


	@Override
	protected Map<String, String> getDefaultReplacements(CommandSender recipient) {

		Map<String, String> replacements = new HashMap<>();

		// strip color codes
		replacements.put("%ITEM_NAME%", ChatColor.stripColor(getItemName()));
		replacements.put("%PLAYER_NAME%", ChatColor.stripColor(recipient.getName()));
		replacements.put("%WORLD_NAME%", ChatColor.stripColor(getWorldName(recipient)));
		replacements.put("%DESTINATION_NAME%", ChatColor.stripColor(getSpawnDisplayName()));
		replacements.put("%TARGET_PLAYER%", "target player");
		replacements.put("%QUANTITY%", "1");
		replacements.put("%MATERIAL%", "unknown");
		replacements.put("%WARMUP_TIME%",
				getTimeString(TimeUnit.SECONDS.toMillis(plugin.getConfig().getInt("teleport-warmup"))));


		// retain color codes
		replacements.put("%player_name%", recipient.getName());
		replacements.put("%world_name%", getWorldName(recipient));
		replacements.put("%item_name%", getItemName());
		replacements.put("%destination_name%", getSpawnDisplayName());
		replacements.put("%target_player%", "target player");

		// if recipient is player, get remaining cooldown time from teleport manager
		if (recipient instanceof Player) {
			replacements.put("%COOLDOWN_TIME%",
					getTimeString(plugin.teleportManager.getCooldownTimeRemaining((Player) recipient)));
		}
		else {
			replacements.put("%COOLDOWN_TIME%", getTimeString(0L));
		}

		return replacements;
	}


	/**
	 * Send message to player
	 *
	 * @param recipient player receiving message
	 * @param messageId message identifier in messages file
	 */
	public void sendMessage(final CommandSender recipient, final MessageId messageId) {

		// get default replacements map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		// send message to recipient
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to player
	 *
	 * @param recipient player receiving message
	 * @param messageId message identifier in messages file
	 * @param quantity  number of items
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final Integer quantity) {

		// get default replacements map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		// set passed quantity in replacements map
		replacements.put("%QUANTITY%", quantity.toString());

		// send message to recipient
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to player
	 *
	 * @param recipient       player recieving message
	 * @param messageId       message identifier in messages file
	 * @param destinationName name of destination
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final String destinationName) {

		// get default replacements map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		// set destination name in replacement map
		replacements.put("%DESTINATION_NAME%", ChatColor.stripColor(destinationName));
		replacements.put("%destination_name%", destinationName);

		// send message to recipient
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to player
	 *
	 * @param recipient       player receiving message
	 * @param messageId       message identifier in messages file
	 * @param quantity        number of items
	 * @param destinationName name of destination
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final Integer quantity,
							final String destinationName) {

		// get default replacements map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		// set quantity in replacement map
		replacements.put("%QUANTITY%", quantity.toString());

		// set destination name in replacement map
		replacements.put("%DESTINATION_NAME%", ChatColor.stripColor(destinationName));
		replacements.put("%destination_name%", destinationName);

		// send message to recipient
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to player
	 *
	 * @param recipient        Player receiving message
	 * @param messageId        message identifier in messages file
	 * @param quantity         number of items
	 * @param destinationName  name of destination
	 * @param targetPlayerName name of player targeted
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final Integer quantity,
							final String destinationName,
							final String targetPlayerName) {

		// get default replacements map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		// set quantity in replacement map
		replacements.put("%QUANTITY%", quantity.toString());

		// set destination name in replacement map
		replacements.put("%DESTINATION_NAME%", ChatColor.stripColor(destinationName));
		replacements.put("%destination_name%", destinationName);

		// set target player name in replacement map
		replacements.put("%TARGET_PLAYER%", ChatColor.stripColor(targetPlayerName));
		replacements.put("%target_player%", targetPlayerName);

		// send message to recipient
		sendMessage(recipient, messageId, replacements);
	}


	public String getInventoryItemName() {
		return messages.getString("ITEM_INFO.INVENTORY_ITEM_NAME");
	}

}
