package com.winterhaven_mc.lodestar;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Implements message manager for <code>LodeStar</code>.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
class MessageManager {

	private final LodeStarMain plugin; // reference to main class
	private ConfigAccessor messages;
	private ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> messageCooldownMap;

	/**
	 * Constructor method for class
	 * 
	 * @param plugin
	 */
	MessageManager(LodeStarMain plugin) {
		
		// create pointer to main class
		this.plugin = plugin;

		// install localization files
		String[] localization_files = {"en-US", "es-ES", "de-DE"};
        installLocalizationFiles(localization_files);
		
		// get configured language
		String language = plugin.getConfig().getString("language");

		// check if localization file for configured language exists, if not then fallback to en-US
		if (!new File(plugin.getDataFolder() + "/language/" + language + ".yml").exists()) {
            plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
            language = "en-US";
        }
		
		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language/" + language + ".yml");
		
		// initalize messageCooldownMap
		messageCooldownMap = new ConcurrentHashMap<UUID,ConcurrentHashMap<String,Long>>();

    }


	/** Send message to player
	 * 
	 * @param sender		Player to message
	 * @param messageID		Identifier of message to send from messages.yml
	 */
    void sendPlayerMessage(CommandSender sender, String messageID) {
		this.sendPlayerMessage(sender, messageID, 1, "", "");
	}

    
    void sendPlayerMessage(CommandSender sender, String messageID, Integer quantity) {
		this.sendPlayerMessage(sender, messageID, quantity, "", "");
	}

    
    void sendPlayerMessage(CommandSender sender, String messageID, String destinationName) {
		this.sendPlayerMessage(sender, messageID, 1, destinationName,"");
	}

    void sendPlayerMessage(CommandSender sender, String messageID, Integer quantity, String destinationName) {
		this.sendPlayerMessage(sender, messageID, quantity, destinationName,"");
    }

	/** Send message to player
	 * 
	 * @param sender		Player to message
	 * @param messageID		Identifier of message to send from messages.yml
	 * @param quantity		number of items
	 * @param destinationName
	 * @param targetPlayerName
	 */	
    void sendPlayerMessage(CommandSender sender,
    		String messageID,
    		Integer quantity,
    		String destinationName,
    		String targetPlayerName) {
    	
		// if message is set to enabled in messages file
		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled")) {

			// set substitution variables defaults			
			String playerName = "console";
			String playerNickname = "console";
			String playerDisplayName = "console";
			String worldName = "unknown world";
			Long remainingTime = 0L;

			// if sender is a player...
			if (sender instanceof Player) {
				
				Player player = (Player) sender;
				Long lastDisplayed = 0L;
				
				// check if player is in message cooldown hashmap
				if (messageCooldownMap.containsKey(player.getUniqueId())) {
					
					// check if messageID is in player's cooldown hashmap
					if (messageCooldownMap.get(player.getUniqueId()).containsKey(messageID)) {
						lastDisplayed = messageCooldownMap.get(player.getUniqueId()).get(messageID);
					}
				}
				
				// if message has repeat delay value and was displayed to player more recently, do nothing and return
				int messageRepeatDelay = messages.getConfig().getInt("messages." + messageID + ".repeat-delay");
				if (lastDisplayed > System.currentTimeMillis() - messageRepeatDelay * 1000) {
					return;
				}
		        
				// if repeat delay value is greater than zero, add entry to messageCooldownMap
		        if (messageRepeatDelay > 0) {
		        	ConcurrentHashMap<String, Long> tempMap = new ConcurrentHashMap<String, Long>();
		        	tempMap.put(messageID, System.currentTimeMillis());
		        	messageCooldownMap.put(player.getUniqueId(), tempMap);
		        }
				
				// assign player dependent variables
		        if (!targetPlayerName.isEmpty()) {
		        	playerName = targetPlayerName;
		        }
	        	playerName = player.getName().replaceAll("[" + ChatColor.COLOR_CHAR + "&][0-9A-Za-zK-Ok-oRr]", "");
	        	playerNickname = player.getPlayerListName().replaceAll("[" + ChatColor.COLOR_CHAR + "&][0-9A-Za-zK-Ok-oRr]", "");
	        	playerDisplayName = player.getDisplayName();
	        	worldName = player.getWorld().getName();
		        remainingTime = plugin.cooldownManager.getTimeRemaining(player);
			}
			
			// get message from file
			String message = messages.getConfig().getString("messages." + messageID + ".string");
	
			// get item name and strip color codes
	        String itemName = getItemName().replaceAll("[" + ChatColor.COLOR_CHAR + "&][0-9A-Za-zK-Ok-oRr]", "");

	        // get warmup value from config file
	        Integer warmupTime = plugin.getConfig().getInt("teleport-warmup");
	        
			// if destination is spawn...
	        // TODO: check this for matching custom spawn name
			if (destinationName.equals(plugin.messageManager.getSpawnDisplayName()) 
					|| destinationName.equals("spawn") ) {

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
			
			// if Multiverse is installed, use Multiverse world alias for world name
			if (plugin.mvEnabled && plugin.mvCore.getMVWorldManager().getMVWorld(worldName) != null) {
				
				// if Multiverse alias is not blank, set world name to alias
				if (!plugin.mvCore.getMVWorldManager().getMVWorld(worldName).getAlias().isEmpty()) {
					worldName = plugin.mvCore.getMVWorldManager().getMVWorld(worldName).getAlias();
				}
			}
	        
			// if quantity is greater than one, use plural item name
			if (quantity > 1) {
				// get plural item name
				itemName = getItemNamePlural();
			}
			
			// do variable substitutions
	        message = message.replace("%itemname%", itemName);
	        message = message.replace("%playername%", playerName);
	        message = message.replace("%playerdisplayname%", playerDisplayName);
	        message = message.replace("%playernickname%", playerNickname);
	        message = message.replace("%worldname%", worldName);
	        message = message.replace("%timeremaining%", remainingTime.toString());
	        message = message.replace("%warmuptime%", warmupTime.toString());
	        message = message.replace("%quantity%", quantity.toString());
	        message = message.replace("%destination%", destinationName);
	        
	        // do variable substitutions, stripping color codes from all caps variables
	        message = message.replace("%ITEMNAME%", ChatColor.stripColor(itemName));
	        message = message.replace("%WORLDNAME%", ChatColor.stripColor(worldName));
	        message = message.replace("%DESTINATION%", ChatColor.stripColor(destinationName));
	        
			// send message to player
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
    }
	
	
	/**
	 * Remove player from message cooldown map
	 * @param player
	 */
	void removePlayerCooldown(Player player) {
		messageCooldownMap.remove(player.getUniqueId());
	}

	
	/** Install list of embedded localization files
	 * 
	 * @param filelist	String list of embedded localizations files to install
	 */
	private void installLocalizationFiles(String[] filelist) {

		for (String filename : filelist) {
			if (!new File(plugin.getDataFolder() + "/language/" + filename + ".yml").exists()) {
				this.plugin.saveResource("language/" + filename + ".yml",false);
				plugin.getLogger().info("Installed localization files for " + filename + ".");
			}
		}
	}

	void reloadMessages() {
		messages.reloadConfig();
	}

	String getItemName() {
		return messages.getConfig().getString("item-name");
	}

	String getItemNamePlural() {
		return messages.getConfig().getString("item-name-plural");
	}

	String getInventoryItemName() {
		return messages.getConfig().getString("inventory-item-name");
	}

	String getSpawnDisplayName() {
		return messages.getConfig().getString("spawn-display-name");
	}

	String getHomeDisplayName() {
		return messages.getConfig().getString("home-display-name");
	}


	List<String> getItemLore() {
		return messages.getConfig().getStringList("item-lore");
	}

}

