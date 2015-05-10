package com.winterhaven_mc.lodestar;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
        installLocalizationFiles();
		
		// get configured language
		String language = plugin.getConfig().getString("language");

		// check if localization file for configured language exists, if not then fallback to en-US
		if (!new File(plugin.getDataFolder() 
				+ File.separator + "language" 
				+ File.separator + language + ".yml").exists()) {
            plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
            language = "en-US";
        }
		
		// instantiate custom configuration manager for language file
		messages = new ConfigAccessor(plugin, "language" + File.separator + language + ".yml");

		// initialize messageCooldownMap
		messageCooldownMap = new ConcurrentHashMap<UUID,ConcurrentHashMap<String,Long>>();
    }


	/**
	 *  Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageID			message identifier in messages file
	 */
    void sendPlayerMessage(CommandSender sender, String messageID) {
		this.sendPlayerMessage(sender, messageID, 1, "", "");
	}

    /**
     * Send message to player
     * 
     * @param sender			player receiving message
     * @param messageID			message identifier in messages file
     * @param quantity			number of items
     */
    void sendPlayerMessage(CommandSender sender, String messageID, Integer quantity) {
		this.sendPlayerMessage(sender, messageID, quantity, "", "");
	}

    /**
     * Send message to player
     * 
     * @param sender			player recieving message
     * @param messageID			message identifier in messages file
     * @param destinationName	name of destination
     */
    void sendPlayerMessage(CommandSender sender, String messageID, String destinationName) {
		this.sendPlayerMessage(sender, messageID, 1, destinationName,"");
	}

    /**
     * Send message to player
     * 
     * @param sender			player receiving message
     * @param messageID			message identifier in messages file
     * @param quantity			number of items
     * @param destinationName	name of destination
     */
    void sendPlayerMessage(CommandSender sender, String messageID, Integer quantity, String destinationName) {
		this.sendPlayerMessage(sender, messageID, quantity, destinationName,"");
    }

	/** Send message to player
	 * 
	 * @param sender			Player receiving message
	 * @param messageID			message identifier in messages file
	 * @param quantity			number of items
	 * @param destinationName	name of destination
	 * @param targetPlayerName	name of player targeted
	 */	
    void sendPlayerMessage(CommandSender sender,
    		String messageID,
    		Integer quantity,
    		String destinationName,
    		String targetPlayerName) {
    	
		// if message is enabled in messages file
		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled")) {

			// set substitution variables defaults			
			String playerName = "console";
			String playerNickname = "console";
			String playerDisplayName = "console";
			String worldName = "world";
			Long remainingTime = 0L;
			
			if (targetPlayerName == null || targetPlayerName.isEmpty()) {
				targetPlayerName = "player";
			}

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
	        	playerName = player.getName();
	        	playerNickname = player.getPlayerListName();
	        	playerDisplayName = player.getDisplayName();
	        	worldName = player.getWorld().getName();
		        remainingTime = plugin.cooldownManager.getTimeRemaining(player);
			}
			
			// get message from file
			String message = messages.getConfig().getString("messages." + messageID + ".string");
	
			// get item name and strip color codes
	        String itemName = getItemName();

	        // get warmup value from config file
	        Integer warmupTime = plugin.getConfig().getInt("teleport-warmup");
	        
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
			
			// if destination is home, get home display name from language file
			if (key.equals(Destination.deriveKey("home"))
					|| key.equals(Destination.deriveKey(plugin.messageManager.getHomeDisplayName()))) {
				destinationName = getHomeDisplayName();
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
	        message = message.replace("%targetplayer%", targetPlayerName);
	        
	        // do variable substitutions, stripping color codes from all caps variables
	        message = message.replace("%ITEMNAME%", 
	        		ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',itemName)));
	        message = message.replace("%PLAYERNAME%", 
	        		ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerName)));
	        message = message.replace("%PLAYERNICKNAME%", 
	        		ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerNickname)));
	        message = message.replace("%WORLDNAME%", 
	        		ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',worldName)));
	        message = message.replace("%DESTINATION%", 
	        		ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',destinationName)));
	        message = message.replace("%TARGETPLAYER%", 
	        		ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',targetPlayerName)));
	        
	        // no stripping of color codes necessary, but do variable substitutions anyhow
	        // in case all caps variables were used
	        message = message.replace("%PLAYERDISPLAYNAME%", playerDisplayName);
	        message = message.replace("%TIMEREMAINING%", remainingTime.toString());
	        message = message.replace("%WARMUPTIME%", warmupTime.toString());
	        message = message.replace("%QUANTITY%", quantity.toString());
	        
			// send message to player
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
    }
	
	
    /**
     * Play sound effect for action
     * @param sender
     * @param soundId
     */
	void playerSound(CommandSender sender, String soundId) {
	
		if (sender instanceof Player) {
			playerSound((Player)sender,soundId);
		}
	}


	/**
	 * Play sound effect for action
	 * @param player
	 * @param soundId
	 */
	void playerSound(Player player, String soundId) {
		
		// if sound effects are disabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("sound-effects")) {
			return;
		}
		
		// if sound is set to enabled in messages file
		if (plugin.getConfig().getBoolean("sounds." + soundId + ".enabled")) {
			
			// get player only setting from config file
			boolean playerOnly = plugin.getConfig().getBoolean("sounds." + soundId + ".player-only");
	
			// get sound name from config file
			String soundName = plugin.getConfig().getString("sounds." + soundId + ".sound");
	
			// get sound volume from config file
			float volume = (float) plugin.getConfig().getDouble("sounds." + soundId + ".volume");
			
			// get sound pitch from config file
			float pitch = (float) plugin.getConfig().getDouble("sounds." + soundId + ".pitch");
	
			if (playerOnly) {
				player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
			}
			else {
				player.getWorld().playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
			}
		}
	}


	/**
	 * Remove player from message cooldown map
	 * @param player
	 */
	void removePlayerCooldown(Player player) {
		messageCooldownMap.remove(player.getUniqueId());
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

	
	void reloadMessages() {
		installLocalizationFiles();
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

