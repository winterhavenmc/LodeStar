package com.winterhaven_mc.lodestar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
//import org.bukkit.Sound;
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
	
	private final static ChatColor helpColor = ChatColor.YELLOW;
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
		
		String subcmd = "";
		
		// get subcommand
		if (args.length > 0) {
			subcmd = args[0];
		}
		// if no arguments, display usage
		else {
			displayUsage(sender,"all");
			return true;
		}
		
		// status command
		if (subcmd.equalsIgnoreCase("status")) {
			return statusCommand(sender);
		}

		// reload command
		if (subcmd.equalsIgnoreCase("reload")) {
			return reloadCommand(sender,args);
		}

		// give command
		if (subcmd.equalsIgnoreCase("give")) {
			return giveCommand(sender,args);
		}
		
		// destroy command
		if (subcmd.equalsIgnoreCase("destroy")) {
			return destroyCommand(sender,args);
		}
		
		//set command
		if (subcmd.equalsIgnoreCase("set")) {
			return setCommand(sender,args);
		}
		
		// delete command
		if (subcmd.equalsIgnoreCase("delete") || subcmd.equalsIgnoreCase("unset")) {
			return deleteCommand(sender,args);
		}
		
		// bind command
		if (subcmd.equalsIgnoreCase("bind")) {
			return bindCommand(sender,args);
		}
		
		// list command
		if (subcmd.equalsIgnoreCase("list")) {
			return listCommand(sender,args);
		}
		
		// help command
		if (subcmd.equalsIgnoreCase("help")) {
			return helpCommand(sender,args);
		}
		
		plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-command");
		plugin.messageManager.playerSound(sender, "command-fail");
		displayUsage(sender,"help");
		return true;
	}


	/**
	 * Display plugin settings
	 * @param sender
	 * @return boolean
	 */
	boolean statusCommand (CommandSender sender) {
		
		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission("lodestar.status")) {
			plugin.messageManager.sendPlayerMessage(sender, "permission-denied-status");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		// output config settings
		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + " Version: " + ChatColor.RESET + versionString);
		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
		sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.messageManager.getLanguage());
		sender.sendMessage(ChatColor.GREEN + "Storage type: " + ChatColor.RESET + plugin.dataStore.getName());
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
	
	
	/**
	 * Reload plugin settings
	 * @param sender
	 * @param args
	 * @return boolean
	 */
	boolean reloadCommand(CommandSender sender, String args[]) {
		
		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission("lodestar.reload")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-reload");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		String subcmd = args[0];
		
		// argument limits
		int minArgs = 1;
		int maxArgs = 1;
		
		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			displayUsage(sender, subcmd);
			return true;
		}

		// check max arguments
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-over");
			displayUsage(sender, subcmd);
			return true;
		}
		
		// reload main configuration
		plugin.reloadConfig();

		// update enabledWorlds list
		updateEnabledWorlds();
		
		// reload messages
		plugin.messageManager.reload();

		// reload datastore
		DataStoreFactory.reload();
		
		// set debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// send reloaded message
		plugin.messageManager.sendPlayerMessage(sender,"command-success-reload");
		return true;
	}
	
	
	/**
	 * Destroy a LodeStar item in hand
	 * @param sender
	 * @param args
	 * @return boolean
	 */
	boolean destroyCommand(CommandSender sender, String args[]) {
		
		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-console");
			return true;
		}
		
		// check that sender has permission
		if (!sender.hasPermission("lodestar.destroy")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-destroy");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		String subcmd = args[0];
		
		// argument limits
		int minArgs = 1;
		int maxArgs = 1;

		// if too few arguments, send error and usage message
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		// if too many arguments, send error and usage message
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-over");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;				
		}
		
		Player player = (Player) sender;
		ItemStack playerItem = player.getItemInHand();
		
		// check that player is holding a LodeStar item
		if (!plugin.utilities.isLodeStar(playerItem)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-item");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		int quantity = playerItem.getAmount();
		String destinationName = plugin.utilities.getDestinationName(playerItem);
		playerItem.setAmount(0);
		player.setItemInHand(playerItem);
		plugin.messageManager.sendPlayerMessage(sender,"command-success-destroy",quantity,destinationName);
		plugin.messageManager.playerSound(player,"command-success-destroy");
		return true;
	}
	
	
	boolean setCommand(CommandSender sender, String args[]) {
		
		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-console");
			return true;
		}
		
		String subcmd = args[0];
		
		int minArgs = 2;
		int maxArgs = 2;
		
		// check for permission
		if (!sender.hasPermission("lodestar.set")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-set");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		// check max arguments
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-over");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		Player player = (Player) sender;
		Location location = player.getLocation();
		
		// set destinationName to passed argument with underscores replaced by spaces
		String destinationName = args[1].replace('_', ' ');

		// check if destination name is a reserved name
		if (plugin.utilities.isNameReserved(destinationName)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-set-reserved",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// check if destination name parses to an integer
		boolean isInteger = true;
		try {
			Integer.parseInt(Destination.deriveKey(destinationName));
		} catch (NumberFormatException e) {
			isInteger = false;
		}
		
		// send invalid destination error message if name parses to an integer
		if (isInteger) {
			plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// check if warp name exists and if so if player has overwrite permission
		Destination destination = plugin.dataStore.getRecord(destinationName);

		// check for overwrite permission destination already exists
		if (destination != null && sender.hasPermission("lodestar.set.overwrite")) {
			plugin.messageManager.sendPlayerMessage(sender, "permission-denied-overwrite",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// create destination object
		destination = new Destination(destinationName,location);
		
		// store destination object
		plugin.dataStore.putRecord(destination);

		// send success message to player
		plugin.messageManager.sendPlayerMessage(sender,"command-success-set",destinationName);
		
		// play sound effect
		plugin.messageManager.playerSound(sender,"command-success-set");
		return true;
	}
	
	
	/**
	 * Remove named destination
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean deleteCommand(CommandSender sender,String args[]) {

		// check for permission
		if (!sender.hasPermission("lodestar.delete")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-delete");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		String subcmd = args[0];
		int minArgs = 2;
		int maxArgs = 2;

		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			displayUsage(sender, subcmd);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		// check max arguments
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-over");
			displayUsage(sender, subcmd);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		String destinationName = args[1];
		String key = Destination.deriveKey(destinationName);
		
		// test that destination name is not reserved name
		if (plugin.utilities.isNameReserved(destinationName)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-delete-reserved",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// test that destination name is valid
		if (!plugin.utilities.isValidDestination(destinationName)) {
			plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// remove destination record from storage
		plugin.dataStore.deleteRecord(key);
		
		// send success message to player
		plugin.messageManager.sendPlayerMessage(sender,"command-success-delete",destinationName);
		
		// play sound effect
		plugin.messageManager.playerSound(sender, "command-success-delete");
		return true;
	}
	
	/**
	 * Bind item in hand to destination
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean bindCommand(CommandSender sender,String args[]) {
		
		// command sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-console");
			return true;
		}
		
		// check sender has permission
		if (!sender.hasPermission("lodestar.bind")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-bind");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		String subcmd = args[0];
		int minArgs = 2;
		int maxArgs = 2;
		
		// check minimum arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		// check maximum arguments
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-args-count-over");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		Player player = (Player) sender;
		String destinationName = args[1];
		
		// test that destination name is valid
		if (!plugin.utilities.isValidDestination(destinationName)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-destination",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// get player item in hand
		ItemStack playerItem = player.getItemInHand();
		
		// if default-item-only configured true, check that item in hand has default material and data 
		if (plugin.getConfig().getBoolean("default-item-only")
				&& !sender.hasPermission("lodestar.default-override")) {
			if (!plugin.utilities.isDefaultItem(playerItem)) {
				plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-item",destinationName);
				plugin.messageManager.playerSound(sender, "command-fail");
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
		
		// send success message
		plugin.messageManager.sendPlayerMessage(sender, "command-success-bind", destinationName);
		
		// play sound effect
		plugin.messageManager.playerSound(sender, "command-success-bind");
		return true;
	}

	
	/**
	 * List LodeStar destination names
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean listCommand(CommandSender sender,String args[]) {
		
		// if command sender does not have permission to list destinations, output error message and return true
		if (!sender.hasPermission("lodestar.list")) {
			plugin.messageManager.sendPlayerMessage(sender, "permission-denied-list");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		String subcmd = args[0];
		// argument limits
		int minArgs = 1;
		int maxArgs = 2;
		
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender, "command-args-count-over");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		int page = 1;
		
		if (args.length == 2) {
			try {
				page = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				// second argument not a page number, let default of 1 stand
			}
		}
		page = Math.max(1, page);

		int itemsPerPage = 20;
		
		List<String> displayNames = plugin.dataStore.getAllKeys();
		
		int pageCount = (displayNames.size() / itemsPerPage) + 1;
		if (page > pageCount) {
			page = pageCount;
		}
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page*itemsPerPage),displayNames.size());
		
		List<String> displayRange = displayNames.subList(startIndex, endIndex);
		
		sender.sendMessage(ChatColor.DARK_AQUA + "page " + page + " of " + pageCount);
		sender.sendMessage(ChatColor.AQUA + displayRange.toString().substring(1, displayRange.toString().length() - 1));
		return true;
	}
	
	
	/**
	 * Display help message for commands
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean helpCommand(CommandSender sender, String args[]) {

		String command = "help";
		
		if (args.length > 1) {
			command = args[1]; 
		}
		
		String helpMessage = "That is not a valid LodeStar command.";
		
		if (command.equalsIgnoreCase("bind")) {
			helpMessage = "Binds a LodeStar destination to an item in hand.";
		}
		if (command.equalsIgnoreCase("give")) {
			helpMessage = "Give LodeStar items directly to players.";
		}
		if (command.equalsIgnoreCase("delete")) {
			helpMessage = "Removes a LodeStar destination.";
		}
		if (command.equalsIgnoreCase("destroy")) {
			helpMessage = "Destroys a LodeStar item in hand.";
		}
		if (command.equalsIgnoreCase("help")) {
			helpMessage = "Displays help for LodeStar commands.";
		}
		if (command.equalsIgnoreCase("list")) {
			helpMessage = "Displays a list of all LodeStar destinations.";
		}
		if (command.equalsIgnoreCase("reload")) {
			helpMessage = "Reloads the configuration without needing to restart the server.";
		}
		if (command.equalsIgnoreCase("set")) {
			helpMessage = "Creates a LodeStar destination at current player location.";
		}
		if (command.equalsIgnoreCase("status")) {
			helpMessage = "Displays current configuration settings.";
		}
		sender.sendMessage(helpColor + helpMessage);
		displayUsage(sender,command);
		return true;
	}
	
	
	/**
	 * Give a LodeStar item to a player
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean giveCommand(CommandSender sender, String args[]) {
		
		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission("lodestar.give")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-give");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
	
		String subcmd = args[0];
		
		// argument limits
		int minArgs = 2;
		int maxArgs = 5;
	
		// if too few arguments, send error and usage message
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		// if too many arguments, send error and usage message
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-over");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;				
		}
		
		// get required argument target player name
		String targetPlayerName = args[1];
		int quantity = 1;
	
		// try to match target player name to currently online player
		Player targetPlayer = matchPlayer(sender, targetPlayerName);
	
		// if no match, do nothing and return (message was output by matchPlayer method)
		if (targetPlayer == null) {
			return true;
		}
		
		// if only two arguments, give 1 of item in hand to target player if item is LodeStar
		// or if item in hand is not a LodeStar item, give 1 default LodeStar item with destination spawn 
		if (args.length == 2) {
			
			ItemStack itemStack = null;
			
			// if sender is player, clone item in hand to itemStack
			if (sender instanceof Player) {
				Player player = (Player) sender;
				itemStack = player.getItemInHand().clone();
				itemStack.setAmount(quantity);
			}
	
			// if itemStack is not a LodeStar item, create new default item with destination spawn
			if (!plugin.utilities.isLodeStar(itemStack)) {
				itemStack = plugin.utilities.createItem("spawn",quantity);
			}
			
			// give item stack to target player
			giveItem(sender, targetPlayer, itemStack);
			
			// play sounds to giver and receiver
			plugin.messageManager.playerSound(sender,"command-success-give-sender");
			plugin.messageManager.playerSound(targetPlayer,"command-success-give-target");
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
					plugin.messageManager.playerSound(sender, "command-fail");
					return true;
				}
				
				// create default item stack with amount 1 to give to target player
				ItemStack itemStack = plugin.utilities.createItem(destinationName, 1);
				
				// give item stack to target player
				giveItem(sender, targetPlayer, itemStack);
				return true;
			}
			
			// third arg IS quantity; send target player item in hand with quantity
			
			ItemStack itemStack = null;
			
			// if sender is a player clone item in hand to itemStack
			if (sender instanceof Player) {
				Player player = (Player) sender;
				itemStack = player.getItemInHand().clone();
				itemStack.setAmount(quantity);
			}
			
			// if itemStack is not a LodeStar item, create default item with destination spawn
			if (!plugin.utilities.isLodeStar(itemStack)) {
				itemStack = plugin.utilities.createItem("spawn", quantity);
			}
			
			// give item stack to target player
			giveItem(sender, targetPlayer, itemStack);
			
			return true;				
		}
		
		// if four arguments, args[2] must be destination, args[3] may be quantity or item material
		if (args.length == 4) {
			
			String destinationName = args[2];
	
			// check that destination name is valid
			if (!plugin.utilities.isValidDestination(destinationName)) {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", destinationName);
				plugin.messageManager.playerSound(sender, "command-fail");
				return true;
			}
			
			// get destination key
			String key = Destination.deriveKey(destinationName);
			
			// get formatted destination name
			destinationName = plugin.utilities.getDestinationName(key);
			
			Material material = null;
			byte materialDataByte = 0;
	
			// try to parse arg[3] as integer	
			try {
				quantity = Integer.parseInt(args[3]);
			}
			catch (NumberFormatException e) {

				// arg[3] not an integer, must be item material

				// set quantity to one
				quantity = 1;

				// if default-item-only is configured true, skip material argument
				if (!plugin.getConfig().getBoolean("default-item-only")) {

					// get argument and optional data from args[3]
					String[] materialElements = args[3].split("\\s*:\\s*");

					// try to match material
					if (materialElements.length > 0) {
						material = Material.matchMaterial(materialElements[0]);
					}
					
					// if data set in args[3] try to parse as byte; set to zero if it doesn't parse
					if (materialElements.length > 1) {
						try {
							materialDataByte = Byte.parseByte(materialElements[1]);
						}
						catch (NumberFormatException e2) {
							materialDataByte = (byte) 0;
						}
					}
				}
			}
			
			// if no material matched try to match default material
			if (material == null) {
				material = Material.matchMaterial(plugin.getConfig().getString("default-material"));
			}
			
			// is still no match set to nether star
			if (material == null) {
				material = Material.NETHER_STAR;
			}
			
			// create item stack with configured material and data
			ItemStack itemStack = new ItemStack(material,quantity,materialDataByte);
	
			// set item meta data
			plugin.utilities.setMetaData(itemStack, destinationName);
	
			itemStack.setAmount(quantity);
	
			// give item stack to target player
			giveItem(sender, targetPlayer, itemStack);
			return true;
		}
			
		// if five args, args[2] must be destination, args[3] must be material, args[4] must be quantity
		if (args.length == 5) {
	
			String destinationName = args[2];
	
			// check that destination name is valid
			if (!plugin.utilities.isValidDestination(destinationName)) {
				plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-destination",destinationName);
				plugin.messageManager.playerSound(sender,"command-fail");
				return true;
			}
			
			// get destination key
			String key = Destination.deriveKey(destinationName);
			
			// get formatted destination name
			destinationName = plugin.utilities.getDestinationName(key);
	
			// args[3] must be item material

			Material material = null;
			byte materialDataByte = 0;
			
			// if default-item-only is configured true, skip material argument
			if (!plugin.getConfig().getBoolean("default-item-only")) {

				// get argument and optional data from args[3]
				String[] materialElements = args[3].split("\\s*:\\s*");

				// try to match material
				if (materialElements.length > 0) {
					material = Material.matchMaterial(materialElements[0]);
				}
				
				// if data set in args[3] try to parse as byte; set to zero if it doesn't parse
				if (materialElements.length > 1) {
					try {
						materialDataByte = Byte.parseByte(materialElements[1]);
					}
					catch (NumberFormatException e2) {
						materialDataByte = (byte) 0;
					}
				}
			}

			// if no material matched try to match default material from config
			if (material == null) {
				String[] materialElements = plugin.getConfig().getString("default-material").split("\\s*:\\s*");
			
				// try to match material
				if (materialElements.length > 0) {
					material = Material.matchMaterial(materialElements[0]);
				}
				
				// try to match material data
				if (materialElements.length > 1) {
					try {
						materialDataByte = Byte.parseByte(materialElements[1]);
					}
					catch (NumberFormatException e2) {
						materialDataByte = (byte) 0;
					}
				}
			}

			// if still no match set to nether star
			if (material == null) {
				material = Material.NETHER_STAR;
			}

			// try to parse arg[4] as integer	
			try {
				quantity = Integer.parseInt(args[4]);
			}
			catch (NumberFormatException e) {
				
				// send invalid quantity message to player
				plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-quantity");
				plugin.messageManager.playerSound(sender, "command-fail");
				return true;
			}
	
			// create item stack with configured material and data
			ItemStack itemStack = new ItemStack(material,quantity,materialDataByte);
	
			// set item meta data
			plugin.utilities.setMetaData(itemStack, destinationName);
	
			itemStack.setAmount(quantity);
	
			// try to give item stack to target player
			giveItem(sender, targetPlayer, itemStack);
		}
		return true;
	}


	/**
	 * Helper method for give command
	 * @param giver
	 * @param targetPlayer
	 * @param itemStack
	 * @return
	 */
	boolean giveItem(CommandSender giver, Player targetPlayer, ItemStack itemStack) {
	
		String key = plugin.utilities.getKey(itemStack);
		int quantity = itemStack.getAmount();
	
		// test that item is a LodeStar item
		if (!plugin.utilities.isLodeStar(itemStack)) {
			String destinationName = plugin.utilities.getDestinationName(key);
			plugin.messageManager.sendPlayerMessage(giver,"command-fail-invalid-item",destinationName);
			plugin.messageManager.playerSound(giver, "command-fail");
			return true;
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
			plugin.messageManager.sendPlayerMessage(giver,"command-fail-give-inventory-full",quantity);
			return false;
		}
	
		// subtract noFitCount from quantity
		quantity = quantity - noFitCount;
	
		// get destination display name
		String destinationName = plugin.utilities.getDestinationName(key);
	
		// don't display messages if giving item to self
		if (!giver.getName().equals(targetPlayer.getName())) {
			
			// send message and play sound to giver
			plugin.messageManager.sendPlayerMessage(giver, "command-success-give",quantity, 
					destinationName,targetPlayer.getName());
			
			// if giver is in game, play sound
			if (giver instanceof Player) {
				plugin.messageManager.playerSound(giver, "command-success-give-sender");
			}
			
			// send message to target player
			CommandSender targetSender = (CommandSender) targetPlayer;
			plugin.messageManager.sendPlayerMessage(targetSender, "command-success-give-target",quantity,
					destinationName,giver.getName());
		}
		// play sound to target player
		plugin.messageManager.playerSound(targetPlayer, "command-success-give-target");
		return true;
	}


	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	ArrayList<String> getEnabledWorlds() {
		return this.enabledWorlds;
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


	/**
	 * Display command usage
	 * @param sender
	 * @param command
	 */
	void displayUsage(CommandSender sender, String command) {
	
		if (command.isEmpty() || command.equalsIgnoreCase("help")) {
			command = "all";
		}
		if ((command.equalsIgnoreCase("status")	
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.status")) {
			sender.sendMessage(usageColor + "/lodestar status");
		}
		if ((command.equalsIgnoreCase("reload") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.reload")) {
			sender.sendMessage(usageColor + "/lodestar reload");
		}
		if ((command.equalsIgnoreCase("destroy") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.destroy")) {
			sender.sendMessage(usageColor + "/lodestar destroy");
		}
		if ((command.equalsIgnoreCase("set") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.set")) {
			sender.sendMessage(usageColor + "/lodestar set <destination>");
		}
		if ((command.equalsIgnoreCase("delete") 
				|| command.equalsIgnoreCase("unset") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.delete")) {
			sender.sendMessage(usageColor + "/lodestar delete <destination>");
		}
		if ((command.equalsIgnoreCase("help") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.help")) {
			sender.sendMessage(usageColor + "/lodestar help [command]");
		}
		if ((command.equalsIgnoreCase("list") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.list")) {
			sender.sendMessage(usageColor + "/lodestar list [page]");
		}
		if ((command.equalsIgnoreCase("bind") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.bind")) {
			sender.sendMessage(usageColor + "/lodestar bind <destination>");
		}
		if ((command.equalsIgnoreCase("give") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.give")) {
			if (plugin.getConfig().getBoolean("default-item-only")) {
				sender.sendMessage(usageColor + "/lodestar give <player> [destination] [amount]");				
			}
			else {
				sender.sendMessage(usageColor + "/lodestar give <player> [<destination> [material][:data]] [amount]");
			}
		}
	}
}
