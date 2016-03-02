package com.winterhaven_mc.lodestar;

import java.util.ArrayList;
import java.util.Arrays;
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
		// if no arguments, display usage for all commands
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
		String versionString = plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);
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
		ItemStack playerItem = player.getInventory().getItemInMainHand();
		
		// check that player is holding a LodeStar item
		if (!plugin.utilities.isLodeStar(playerItem)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-item");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		int quantity = playerItem.getAmount();
		String destinationName = plugin.utilities.getDestinationName(playerItem);
		playerItem.setAmount(0);
		player.getInventory().setItemInMainHand(playerItem);
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
		
		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);
		
		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;
		
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
		
		Player player = (Player) sender;
		Location location = player.getLocation();
		
		// set destinationName to passed argument
		String destinationName = join(arguments);
		
		// check if destination name is a reserved name
		if (plugin.utilities.isReservedName(destinationName)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-set-reserved",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// check if destination name exists and if so if player has overwrite permission
		Destination destination = plugin.dataStore.getRecord(destinationName);

		// check for overwrite permission if destination already exists
		if (destination != null && sender.hasPermission("lodestar.set.overwrite")) {
			plugin.messageManager.sendPlayerMessage(sender, "permission-denied-overwrite",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// send warning message if name begins with a number
		if (Destination.deriveKey(destinationName).matches("^\\d*_.*")) {
			plugin.messageManager.sendPlayerMessage(sender, "command-warn-set-numeric-prefix",destinationName);
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
		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);
		
		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			displayUsage(sender, subcmd);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		String destinationName = join(arguments);
		String key = Destination.deriveKey(destinationName);
		
		// test that destination name is not reserved name
		if (plugin.utilities.isReservedName(destinationName)) {
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
		
		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);
		
		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;
		
		// check minimum arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		Player player = (Player) sender;
		String destinationName = join(arguments);
		
		// test that destination name is valid
		if (!plugin.utilities.isValidDestination(destinationName)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-invalid-destination",destinationName);
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}
		
		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();
		
		// if default-item-only configured true, check that item in hand has default material and data 
		if (plugin.getConfig().getBoolean("default-material-only")
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
			plugin.messageManager.sendPlayerMessage(sender, "command-fail-args-count-over");
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

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("lodestar.help")) {
			plugin.messageManager.sendPlayerMessage(sender, "permission-denied-help");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

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
	@SuppressWarnings("deprecation")
	boolean giveCommand(CommandSender sender, String args[]) {
		
		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission("lodestar.give")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-give");
			plugin.messageManager.playerSound(sender, "command-fail");
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);
		
		// remove subcmd from ArrayList
		arguments.remove(0);

		// argument limits
		int minArgs = 2;

		// if too few arguments, send error and usage message
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
			plugin.messageManager.playerSound(sender, "command-fail");
			displayUsage(sender, subcmd);
			return true;
		}
		
		// get required argument target player name
		String targetPlayerName = arguments.get(0);
		
		// remove targetPlayerName from ArrayList
		arguments.remove(0);
	
		// try to match target player name to currently online player
		Player targetPlayer = matchPlayer(sender, targetPlayerName);
	
		// if no match, do nothing and return (message was output by matchPlayer method)
		if (targetPlayer == null) {
			return true;
		}
		
		//------------------------
		
		// set destinationName to empty string
		String destinationName = "";
		
		// set default quantity
		int quantity = 1;
		
		Material material = null;
		byte materialDataByte = 0;

		// try to parse first argument as integer quantity
		if (!arguments.isEmpty()) {
			try {
				quantity = Integer.parseInt(arguments.get(0));
				
				// remove argument if no exception thrown
				arguments.remove(0);
			}
			catch (NumberFormatException e) {
				// not an integer, do nothing
			}
		}
		
		// if no remaining arguments, check if item in hand is LodeStar item
		if (arguments.isEmpty()) {
			
			// if sender is not player, send args-count-under error message
			if (!(sender instanceof Player)) {
				plugin.messageManager.sendPlayerMessage(sender,"command-fail-args-count-under");
				displayUsage(sender, subcmd);
				return true;
			}
			
			Player player = (Player) sender;			
			ItemStack playerItem = player.getItemInHand().clone();
			
			// if item in hand is a LodeStar item, set destination and material from item
			if (plugin.utilities.isLodeStar(playerItem)) {
				
				destinationName = plugin.utilities.getDestinationName(playerItem);
				material = playerItem.getType();
				materialDataByte = playerItem.getData().getData();
			}			
		}

		// try to parse all remaining arguments as destinationName
		if (!arguments.isEmpty()) {

			String testName = join(arguments);

			// if resulting name is valid destination, set to destinationName
			if (plugin.utilities.isValidDestination(testName)) {
				destinationName = testName;

				// set arguments to empty list
				arguments.clear();
			}
		}
		
		// try to parse next argument as material
		if (!arguments.isEmpty()) {
			String materialElements[] = arguments.get(0).split("\\s*:\\s*");

			// try to match material
			if (materialElements.length > 0) {
				material = Material.matchMaterial(materialElements[0]);
			}

			// if data set, try to parse as byte; set to zero if it doesn't parse
			if (materialElements.length > 1) {
				try {
					materialDataByte = Byte.parseByte(materialElements[1]);
				}
				catch (NumberFormatException e2) {
					materialDataByte = (byte) 0;
				}
			}
			// if material matched, remove argument from list
			if (material != null) {
				arguments.remove(0);
			}
		}
		
		// try to parse all remaining arguments as destinationName
		if (!arguments.isEmpty()) {
			String testName = join(arguments);

			// if resulting name is valid destination, set to destinationName
			if (plugin.utilities.isValidDestination(testName)) {
				destinationName = testName;

				// set arguments to empty list
				arguments.clear();
			}
			// else given destination is invalid (but not blank), so send error message
			else {
				plugin.messageManager.sendPlayerMessage(sender, "command-fail-invalid-destination", testName);
				plugin.messageManager.playerSound(sender, "command-fail");
				return true;
			}
		}		
		
		// if no destination name set, set destination to spawn
		if (destinationName.isEmpty()) {
				destinationName = "spawn";
		}
		
		// if no material set or default-material-only configured true, try to parse material from config
		if (material == null || plugin.getConfig().getBoolean("default-material-only")) {
			
			String materialElements[] = plugin.getConfig().getString("default-material").split("\\s*:\\s*");

			// try to match material
			if (materialElements.length > 0) {
				material = Material.matchMaterial(materialElements[0]);
			}

			// if data set, try to parse as byte; set to zero if it doesn't parse
			if (materialElements.length > 1) {
				try {
					materialDataByte = Byte.parseByte(materialElements[1]);
				}
				catch (NumberFormatException e2) {
					materialDataByte = (byte) 0;
				}
			}
		}
		// if still no material match, set to nether star
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// create item stack with material, quantity and data		
		ItemStack itemStack = new ItemStack(material, quantity, (short) 0, materialDataByte);

		// set item metadata on item stack
		plugin.utilities.setMetaData(itemStack, destinationName);
		
		// give item stack to target player
		giveItem(sender, targetPlayer, itemStack);
		
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
		int maxGiveAmount = plugin.getConfig().getInt("max-give-amount");
		
		// check quantity against configured max give amount
		if (maxGiveAmount >= 0) {
			quantity = Math.min(maxGiveAmount, quantity);
			itemStack.setAmount(quantity);
		}
		
		// test that item is a LodeStar item
		if (!plugin.utilities.isLodeStar(itemStack)) {
			plugin.messageManager.sendPlayerMessage(giver,"command-fail-invalid-item");
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
			sender.sendMessage(usageColor + "/lodestar set <destination_name>");
		}
		if ((command.equalsIgnoreCase("delete") 
				|| command.equalsIgnoreCase("unset") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.delete")) {
			sender.sendMessage(usageColor + "/lodestar delete <destination_name>");
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
			sender.sendMessage(usageColor + "/lodestar bind <destination_name>");
		}
		if ((command.equalsIgnoreCase("give") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("lodestar.give")) {
			if (plugin.getConfig().getBoolean("default-item-only")) {
				sender.sendMessage(usageColor + "/lodestar give <player> [quantity] [destination_name]");				
			}
			else {
				sender.sendMessage(usageColor + "/lodestar give <player> [quantity] [material][:data] [destination_name]");
			}
		}
	}


	/**
	 * Join list of strings into one string with spaces
	 * @param stringList
	 * @return
	 */
	private String join(List<String> stringList) {
		
		String returnString = "";
		for (String string : stringList) {
			returnString = returnString + " " + string;
		}
		return returnString.trim();
	}
	
}
