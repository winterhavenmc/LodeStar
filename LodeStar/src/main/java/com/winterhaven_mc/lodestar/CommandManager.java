package com.winterhaven_mc.lodestar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Implements command executor for <code>LodeStar</code> commands.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class CommandManager implements CommandExecutor {
	
	private final static ChatColor usageColor = ChatColor.GOLD;

	private final LodeStarMain plugin; // reference to main class
	private ArrayList<String> enabledWorlds;

	/**
	 * constructor method for <code>CommandManager</code> class
	 * 
	 * @param plugin reference to main class
	 */
	CommandManager(LodeStarMain plugin) {
		
		this.plugin = plugin;
		plugin.getCommand("lodestar").setExecutor(this);
		updateEnabledWorlds();
	}


	/** command executor method for LodeStar
	 * 
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		int minArgs = 0;
		int maxArgs = 5;
		String subcmd = "";
		
		// get subcommand
		if (args.length > 0) {
			subcmd = args[0];
		}
		// if no arguments, set subcmd to status command
		else {
			subcmd = "status";
		}
		
		/*
		 * status command
		 */
		if (subcmd.equalsIgnoreCase("status")) {
			
			// if command sender does not have permission to view status, output error message and return true
			if (!sender.hasPermission("lodestar.status")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-status");
				return true;
			}
			
			minArgs = 0;
			maxArgs = 2;
			
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				return true;
			}
			
			if (args.length > maxArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
				displayUsage(sender, subcmd);
				return true;
			}
			
			// output config settings
			String versionString = this.plugin.getDescription().getVersion();
			sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);
			if (plugin.debug) {
				sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
			}
			sender.sendMessage(ChatColor.GREEN + "Storage type: " + ChatColor.RESET + plugin.dataStore.getName());
			sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.getConfig().getString("language"));
			sender.sendMessage(ChatColor.GREEN + "Default material: " + ChatColor.RESET + plugin.getConfig().getString("default-material"));
			sender.sendMessage(ChatColor.GREEN + "Minimum distance: " + ChatColor.RESET + plugin.getConfig().getInt("minimum-distance"));
			sender.sendMessage(ChatColor.GREEN + "Warmup: " + ChatColor.RESET + plugin.getConfig().getInt("teleport-warmup") + " seconds");
			sender.sendMessage(ChatColor.GREEN + "Cooldown: " + ChatColor.RESET + plugin.getConfig().getInt("teleport-cooldown") + " seconds");
			sender.sendMessage(ChatColor.GREEN + "Shift-click required: " + ChatColor.RESET + plugin.getConfig().getBoolean("shift-click"));
			sender.sendMessage(ChatColor.GREEN + "Cancel on damage/movement/interaction: " + ChatColor.RESET + "[ "
					+ plugin.getConfig().getBoolean("cancel-on-damage") + "/"
					+ plugin.getConfig().getBoolean("cancel-on-movement") + "/"
					+ plugin.getConfig().getBoolean("cancel-on-interaction") + " ]");
			sender.sendMessage(ChatColor.GREEN + "Remove from inventory: " + ChatColor.RESET + plugin.getConfig().getString("remove-from-inventory"));
			sender.sendMessage(ChatColor.GREEN + "Allow in recipes: " + ChatColor.RESET + plugin.getConfig().getBoolean("allow-in-recipes"));
			sender.sendMessage(ChatColor.GREEN + "From nether: " + ChatColor.RESET + plugin.getConfig().getBoolean("from-nether"));
			sender.sendMessage(ChatColor.GREEN + "From end: " + ChatColor.RESET + plugin.getConfig().getBoolean("from-end"));
			sender.sendMessage(ChatColor.GREEN + "Lightning: " + ChatColor.RESET + plugin.getConfig().getBoolean("lightning"));
			sender.sendMessage(ChatColor.GREEN + "Enabled Words: " + ChatColor.RESET + getEnabledWorlds().toString());
			return true;
		}

		/*
		 *  reload command
		 */
		if (subcmd.equalsIgnoreCase("reload")) {

			// if sender does not have permission to reload config, send error message and return true
			if (!sender.hasPermission("lodestar.reload")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-reload");
				return true;
			}

			// argument limits
			minArgs = 1;
			maxArgs = 1;
			
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				return true;
			}
			
			if (args.length > maxArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
				displayUsage(sender, subcmd);
				return true;
			}
			
			// get current language setting
			String originalLanguage = plugin.getConfig().getString("language");
			
			// get current datastore type
			DataStoreType originalType = DataStoreType.match(plugin.getConfig().getString("datastore-type"));

			// reload config.yml
			plugin.reloadConfig();

			// update enabledWorlds list
			updateEnabledWorlds();
			
			// if language setting has changed, instantiate new message manager with new language file
			if (!originalLanguage.equals(plugin.getConfig().getString("language"))) {
				plugin.messageManager = new MessageManager(plugin);
			}
			else {
				plugin.messageManager.reloadMessages();
			}
			
			// if datastore type has changed, create new datastore and convert records from existing datastore
			DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("datastore-type"));
			if (!originalType.equals(newType)) {
				
				// create new data store
				DataStore newDataStore = DataStoreFactory.create(newType,plugin.dataStore);
			
				// set plugin.dataStore to reference new data store
				plugin.dataStore = newDataStore;
			}
			
			// send reloaded message to command sender
			plugin.messageManager.sendPlayerMessage(sender, "command-success-reload");
			return true;
		}

		/*
		 *  give command
		 *  /lodestar give <player> <destination> [quantity]
		 */
		if (subcmd.equalsIgnoreCase("give")) {
			
			// if command sender does not have permission to give LodeStars, output error message and return true
			if (!sender.hasPermission("lodestar.give")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-give");
				return true;
			}

			// argument limits
			minArgs = 2;
			maxArgs = 5;

			// if too few arguments, send error and usage message
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				return true;
			}
			
			// if too many arguments, send error and usage message
			if (args.length > maxArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
				displayUsage(sender, subcmd);
				return true;				
			}
			
			// get required argument target player name
			String targetPlayerName = args[1];
			int quantity = 0;

			Player giver = null;
			Player targetPlayer = matchPlayer(sender, targetPlayerName);
			
			if (targetPlayer == null) {
				return true;
			}
			
			// if only two arguments, give 1 of item in hand to target player if item is LodeStar
			if (args.length == 2) {
				
				// check that sender is a player
				if (!(sender instanceof Player)) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-console");
					return true;
				}
				
				giver = (Player) sender;

				// if player is not holding a LodeStar item, send invalid item message and return
				if (!plugin.utilities.isLodeStar(giver.getItemInHand())) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-item");
					return true;
				}
				
				// create item stack with amount 1 to give to target player
				ItemStack giveStack = giver.getItemInHand().clone();
				giveStack.setAmount(1);
				
				// give item stack to target player
				giveItem(sender, targetPlayer, giveStack);
				
				// play sound effects if enabled
				if (plugin.getConfig().getBoolean("sound-effects")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.5F);
					}
					targetPlayer.playSound(targetPlayer.getLocation(), Sound.ITEM_PICKUP, 1, 1);
				}
				return true;				
			}
			
			// if three arguments given, check if third argument is a quantity
			if (args.length == 3) {
				
				// try to parse arg[2] as integer	
				try {
					quantity = Integer.parseInt(args[2]);
				}
				catch (NumberFormatException e) {
					
					// not an integer, must be destination
					String key = Destination.deriveKey(args[2]);
					String destinationName = plugin.utilities.getDestinationName(key);

					// check that destination name is valid
					if (!plugin.utilities.isValidDestination(key)) {
						plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", args[2]);
						playDeniedSound(sender);
						return true;
					}
					
					// create default item stack with amount 1 to give to target player
					ItemStack giveStack = plugin.utilities.createItem(destinationName, 1);
					
					// give item stack to target player
					giveItem(sender, targetPlayer, giveStack);
					
					// play sound effects if enabled
					if (plugin.getConfig().getBoolean("sound-effects")) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.5F);
						}
						targetPlayer.playSound(targetPlayer.getLocation(), Sound.ITEM_PICKUP, 1, 1);
					}
					return true;
				}
				
				// third arg IS quantity; send target player item in hand with quantity
				
				// check that sender is a player
				if (!(sender instanceof Player)) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-console");
					return true;
				}
				
				giver = (Player) sender;

				// if player is not holding a LodeStar item, send invalid item message and return
				if (!plugin.utilities.isLodeStar(giver.getItemInHand())) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-item");
					playDeniedSound(sender);
					return true;
				}
				
				// create item stack of given quantity to give to target player
				ItemStack giveStack = giver.getItemInHand();
				giveStack.setAmount(quantity);
				
				// give item stack to target player
				giveItem(sender, targetPlayer, giveStack);
				
				// play sound effects if enabled
				if (plugin.getConfig().getBoolean("sound-effects")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.5F);
					}
					targetPlayer.playSound(targetPlayer.getLocation(), Sound.ITEM_PICKUP, 1, 1);
				}
				return true;				
			}
			
			// if four arguments, args[2] must be destination, args[3] may be quantity or item material
			if (args.length == 4) {
				
				String destinationName = args[2];

				// check that destination name is valid
				if (!plugin.utilities.isValidDestination(destinationName)) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", destinationName);
					return true;
				}
				
				Material material = null;
				byte materialDataByte = 0;

				// try to parse arg[3] as integer	
				try {
					quantity = Integer.parseInt(args[3]);
				}
				catch (NumberFormatException e) {

					// set quantity to one
					quantity = 1;
					
					// arg[3] not an integer, must be item material
					String[] materialElements = args[3].split("\\s*:\\s*");
					
					// try to match material
					material = Material.matchMaterial(materialElements[0]);
					
					// if data set in args[3] try to parse as byte; set to zero if it doesn't parse
					if (materialElements.length > 1) {
						try {
							materialDataByte = Byte.parseByte(materialElements[1]);
						}
						catch (NumberFormatException e2) {
							materialDataByte = (byte) 0;
						}
					}
					// if no data set default to zero
					else {
						materialDataByte = (byte) 0;
					}

				}
				
				// if no material given try to default material
				if (material == null) {
					material = Material.matchMaterial(plugin.getConfig().getString("default-material"));
				}
				
				// is still no match set to nether star
				if (material == null) {
					material = Material.NETHER_STAR;
				}
				
				// create item stack with configured material and data
				ItemStack giveStack = new ItemStack(material,quantity,materialDataByte);

				// set item meta data
				plugin.utilities.setMetaData(giveStack, destinationName);

				giveStack.setAmount(quantity);

				// give item stack to target player
				giveItem(sender, targetPlayer, giveStack);

				// play sound effects if enabled
				if (plugin.getConfig().getBoolean("sound-effects")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.5F);
					}
					targetPlayer.playSound(targetPlayer.getLocation(), Sound.ITEM_PICKUP, 1, 1);
				}
				return true;
			}
				
			// if five args, args[2] must be destination, args[3] must be material, args[4] must be quantity
			if (args.length == 5) {

				String destinationName = args[2];

				// check that destination name is valid
				if (!plugin.utilities.isValidDestination(destinationName)) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", destinationName);
					playDeniedSound(sender);
					return true;
				}

				// not an integer, must be item material
				String[] materialElements = args[3].split("\\s*:\\s*");

				// try to match material
				Material material = Material.matchMaterial(materialElements[0]);

				// if no match default to nether star
				if (material == null) {
					material = Material.NETHER_STAR;
				}

				// parse material data from config file if present
				byte materialDataByte;

				// if data set in config try to parse as byte; set to zero if it doesn't parse
				if (materialElements.length > 1) {
					try {
						materialDataByte = Byte.parseByte(materialElements[1]);
					}
					catch (NumberFormatException e2) {
						materialDataByte = (byte) 0;
					}
				}
				// if no data set default to zero
				else {
					materialDataByte = (byte) 0;
				}

				// try to parse arg[4] as integer	
				try {
					quantity = Integer.parseInt(args[4]);
				}
				catch (NumberFormatException e) {
					
					// send invalid quantity message to player
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-item");
					playDeniedSound(sender);
					return true;
				}

				// create item stack with configured material and data
				ItemStack giveStack = new ItemStack(material,quantity,materialDataByte);

				// set item meta data
				plugin.utilities.setMetaData(giveStack, destinationName);

				giveStack.setAmount(quantity);

				// give item stack to target player
				giveItem(sender, targetPlayer, giveStack);
				
				// play sound effects if enabled
				if (plugin.getConfig().getBoolean("sound-effects")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.5F);
					}
					targetPlayer.playSound(targetPlayer.getLocation(), Sound.ITEM_PICKUP, 1, 1);
				}
			}
			return true;
		}
		
		/*
		 * destroy command
		 */
		if (subcmd.equalsIgnoreCase("destroy")) {
			
			if (!(sender instanceof Player)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-console");
				return true;
			}
			
			if (!sender.hasPermission("lodestar.destroy")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-destroy");
				playDeniedSound(sender);
				return true;
			}
			
			// argument limits
			minArgs = 1;
			maxArgs = 1;

			// if too few arguments, send error and usage message
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			// if too many arguments, send error and usage message
			if (args.length > maxArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;				
			}
			
			Player player = (Player) sender;
			ItemStack playerItem = player.getItemInHand();
			
			// check that player is holding a LodeStar stack
			if (!plugin.utilities.isLodeStar(playerItem)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-item");
				playDeniedSound(sender);
				return true;
			}
			int quantity = playerItem.getAmount();
			playerItem.setAmount(0);
			player.setItemInHand(playerItem);
			plugin.messageManager.sendPlayerMessage(sender, "command-success-destroy", quantity);
			
			// play sound effect if enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
			}
			return true;
		}
		
		/*
		 * set command
		 */
		if (subcmd.equalsIgnoreCase("set")) {

			if (!(sender instanceof Player)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-console");
				return true;
			}
			
			Player player = (Player) sender;
			
			if (!sender.hasPermission("lodestar.set")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-set");
				playDeniedSound(sender);
				return true;
			}
			
			if (args.length < 2) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			if (args.length > 2) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			Location location = player.getLocation().clone();
			
			String destinationName = args[1];

			// check if destination name is a reserved name
			if (plugin.utilities.nameReserved(destinationName)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-set-reserved", destinationName);
				playDeniedSound(sender);
				return true;
			}
			
			// check if destination name parses to an integer
			boolean isInteger = true;
			try {
				Integer.parseInt(Destination.deriveKey(destinationName));
			} catch (NumberFormatException e) {
				isInteger = false;
			}
			
			if (isInteger) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", destinationName);
				playDeniedSound(sender);
				return true;
			}
			
			// check if warp name exists and if so if player has overwrite permission
			Destination destination = plugin.dataStore.getRecord(destinationName);
			
			if (destination != null && sender.hasPermission("lodestar.set.overwrite")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-overwrite");
				playDeniedSound(sender);
				return true;
			}
			
			// create warp object
			destination = new Destination(destinationName, location);
			
			// store warp object
			plugin.dataStore.putRecord(destination);

			// send success message to player
			plugin.messageManager.sendPlayerMessage(sender, "command-success-set", destinationName);
			
			// play sound effect if enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1.5F);
			}
			return true;
		}
		
		/*
		 * delete command
		 */
		if (subcmd.equalsIgnoreCase("delete")) {
			
			if (!sender.hasPermission("lodestar.delete")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-delete");
				playDeniedSound(sender);
				return true;
			}
			
			minArgs = 2;
			maxArgs = 2;
			
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			if (args.length > maxArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			String destinationName = args[1];
			String key = Destination.deriveKey(destinationName);
			
			// test that destination name is not reserved name
			if (plugin.utilities.nameReserved(destinationName)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-delete-reserved", destinationName);
				playDeniedSound(sender);
				return true;
			}
			
			// test that destination name is valid
			if (!plugin.utilities.isValidDestination(destinationName)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", destinationName);
				playDeniedSound(sender);
				return true;
			}
			
			// remove warp object from storage
			plugin.dataStore.deleteRecord(key);
			
			// send success message to player
			plugin.messageManager.sendPlayerMessage(sender, "command-success-delete", destinationName);
			
			// play sound effect if enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					player.playSound(player.getLocation(), Sound.IRONGOLEM_HIT, 1, 1);
				}
			}
			return true;
		}
		
		/*
		 * bind command
		 */
		if (subcmd.equalsIgnoreCase("bind")) {
			
			if (!(sender instanceof Player)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-console");
				return true;
			}
			
			if (!sender.hasPermission("lodestar.bind")) {
				plugin.messageManager.sendPlayerMessage(sender, "permission-denied-bind");
				playDeniedSound(sender);
				return true;
			}
			
			minArgs = 2;
			maxArgs = 2;
			
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-args-count-under");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			if (args.length > maxArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-args-count-over");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			Player player = (Player) sender;
			String destinationName = args[1];
			
			// test that destination name is valid
			if (!plugin.utilities.isValidDestination(destinationName)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination");
				playDeniedSound(sender);
				return true;
			}
			
			// get player item in hand
			ItemStack playerItem = player.getItemInHand();
			
			// test that item in hand is a LodeStar item if default-item-only configured
			if (plugin.getConfig().getBoolean("default-item-only")) {
				if (!plugin.utilities.isLodeStar(playerItem)) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-item");
					playDeniedSound(sender);
					return true;
				}
			}
			
			// try to get formatted destination name from storage
			Destination destination = plugin.dataStore.getRecord(destinationName);
			if (destination != null) {
				destinationName = destination.getDisplayName();
			}
			
			// set destination in item lore
			plugin.utilities.setMetaData(playerItem, destinationName);
			plugin.messageManager.sendPlayerMessage(sender, "command-success-bind", destinationName);
			
			// play sound effect if enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1.75F);
			}
			return true;
		}
		
		/*
		 * list command
		 */
		if (subcmd.equalsIgnoreCase("list")) {

			// argument limits
			minArgs = 1;
			maxArgs = 1;
			
			if (args.length < minArgs) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
				displayUsage(sender, subcmd);
				playDeniedSound(sender);
				return true;
			}
			
			List<String> displayNames = plugin.dataStore.getAllKeys();			
			sender.sendMessage(displayNames.toString());
			return true;
		}
		
		/*
		 * help command
		 */
		if (subcmd.equalsIgnoreCase("help")) {
			displayUsage(sender,"help");
			return true;
		}
		
		plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-command");
		displayUsage(sender,"help");
		return true;
	}

	
	/**
	 * update enabledWorlds ArrayList field from config file settings
	 */
	void updateEnabledWorlds() {
		
		// copy list of enabled worlds from config into enabledWorlds ArrayList field
		this.enabledWorlds = new ArrayList<String>(plugin.getConfig().getStringList("enabled-worlds"));
		
		// if enabledWorlds ArrayList is empty, add all worlds to ArrayList
		if (this.enabledWorlds.isEmpty()) {
			for (World world : plugin.getServer().getWorlds()) {
				enabledWorlds.add(world.getName());
			}
		}
		
		// remove each disabled world from enabled worlds field
		for (String disabledWorld : plugin.getConfig().getStringList("disabled-worlds")) {
			this.enabledWorlds.remove(disabledWorld);
		}
	}
	
	
	Player matchPlayer(CommandSender sender, String targetPlayerName) {
		
		// check all known players for a match
		OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
		for (OfflinePlayer offlinePlayer : offlinePlayers) {
			if (targetPlayerName.equalsIgnoreCase(offlinePlayer.getName())) {
				if (!offlinePlayer.isOnline()) {
					plugin.messageManager.sendPlayerMessage(sender, "command-fail-player-not-online");
					return null;
				}
			}
		}
		
		// try to match a player from given string
		List<Player> playerList = plugin.getServer().matchPlayer(targetPlayerName);
		Player targetPlayer = null;
		
		// if only one matching player, use it, otherwise send error message (no match or more than 1 match)
		if (playerList.size() == 1) {
			targetPlayer = playerList.get(0);
		}
		else {
			// if unique matching player is not found, send player-not-found message to sender
			plugin.messageManager.sendPlayerMessage(sender, "command-fail-player-not-found");
			return null;
		}
		return targetPlayer;
	}
	
	
	boolean giveItem(CommandSender giver, Player targetPlayer, ItemStack itemStack) {
		
		String key = plugin.utilities.getKey(itemStack);
		int quantity = itemStack.getAmount();
		
		if (plugin.debug) {
			plugin.getLogger().info("[giveItem] Key: " + key);
			plugin.getLogger().info("[giveItem] Quantity: " + quantity);
			plugin.getLogger().info("[giveItem] Giver: " + giver.getName());
			plugin.getLogger().info("[giveItem] Target: " + targetPlayer.getName());

		}
		
		// add specified quantity of LodeStars to player inventory
		HashMap<Integer,ItemStack> noFit = targetPlayer.getInventory().addItem(itemStack);
		
		// count items that didn't fit in inventory
		int noFitCount = 0;
		for (int index : noFit.keySet()) {
			noFitCount += noFit.get(index).getAmount();
		}
		
		// if remaining items equals quantity given, send player-inventory-full message and return
		if (noFitCount == quantity) {
			plugin.messageManager.sendPlayerMessage(giver, "command-fail-give-inventory-full", quantity);
			return false;
		}
		
		// subtract noFitCount from quantity
		quantity = quantity - noFitCount;
		
		// get destination display name
		String destinationName = plugin.utilities.getDestinationName(key);

		if (plugin.debug) {
			plugin.getLogger().info("[giveItem] Destination: " + destinationName);
			plugin.getLogger().info("[giveItem] Quantity: " + quantity);
		}
		
		// send message to giver
		plugin.messageManager.sendPlayerMessage(giver, "command-success-give", quantity, 
				destinationName, targetPlayer.getName());
		
		return true;
	}

	void playDeniedSound(CommandSender sender) {
		
		if (sender instanceof Player) {			
			Player player = (Player) sender;
			
			// play sound effect if enabled
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
			}				
		}
	}
	
	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	ArrayList<String> getEnabledWorlds() {
		return this.enabledWorlds;
	}
	
	void displayUsage(CommandSender sender, String command) {
		
		if (command.isEmpty()) {
			command = "help";
		}
		if ((command.equalsIgnoreCase("status")	|| command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.status")) {
			sender.sendMessage(usageColor + "/lodestar status");
		}
		if ((command.equalsIgnoreCase("reload") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.reload")) {
			sender.sendMessage(usageColor + "/lodestar reload");
		}
		if ((command.equalsIgnoreCase("destroy") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.destroy")) {
			sender.sendMessage(usageColor + "/lodestar destroy");
		}
		if ((command.equalsIgnoreCase("set") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.set")) {
			sender.sendMessage(usageColor + "/lodestar set <name>");
		}
		if ((command.equalsIgnoreCase("delete") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.delete")) {
			sender.sendMessage(usageColor + "/lodestar delete <name>");
		}
		if ((command.equalsIgnoreCase("list") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.list")) {
			sender.sendMessage(usageColor + "/lodestar list");
		}
		if ((command.equalsIgnoreCase("bind") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.bind")) {
			sender.sendMessage(usageColor + "/lodestar bind <destination>");
		}
		if ((command.equalsIgnoreCase("give") || command.equalsIgnoreCase("help"))
				&& sender.hasPermission("lodestar.give")) {
			sender.sendMessage(usageColor + "/lodestar give <player> [<destination> [material][:data]] [amount]");
		}
	}
	
}
