package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.DataStore;
import com.winterhaven_mc.lodestar.storage.Destination;
import com.winterhaven_mc.lodestar.util.LodeStar;

import com.winterhaven_mc.util.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.winterhaven_mc.lodestar.messages.MessageId.*;
import static com.winterhaven_mc.lodestar.messages.Macro.*;


/**
 * Implements command executor for LodeStar commands.
 *
 * @author Tim Savage
 * @version 1.0
 */
public class CommandManager implements CommandExecutor, TabCompleter {

	// reference to main class
	private final PluginMain plugin;

	// reference to language manager
	LanguageManager languageManager;

	// constants for chat colors
	private final static ChatColor helpColor = ChatColor.YELLOW;
	private final static ChatColor usageColor = ChatColor.GOLD;

	// constant list of subcommands
	private final static List<String> subcommands =
			Collections.unmodifiableList(new ArrayList<>(
					Arrays.asList("bind", "give", "delete", "destroy", "list", "set", "status", "reload", "help")));


	/**
	 * constructor method for CommandManager class
	 *
	 * @param plugin reference to main class
	 */
	public CommandManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// set reference to language manager
		languageManager = LanguageManager.getInstance();

		// register this class as command executor
		Objects.requireNonNull(plugin.getCommand("lodestar")).setExecutor(this);

		// register this class as tab completer
		Objects.requireNonNull(plugin.getCommand("lodestar")).setTabCompleter(this);
	}


	/**
	 * Tab completer for LodeStar
	 */
	@Override
	public final List<String> onTabComplete(final CommandSender sender, final Command command,
											final String alias, final String[] args) {

		final List<String> returnList = new ArrayList<>();

		// if first argument
		if (args.length == 1) {

			// return list of valid matching subcommands
			for (String subcommand : subcommands) {
				if (sender.hasPermission("lodestar." + subcommand)
						&& subcommand.startsWith(args[0].toLowerCase())) {
					returnList.add(subcommand);
				}
			}
		}

		// if second argument
		if (args.length == 2) {

			// if subcommand is 'give', return list of online player names
			if (args[0].equalsIgnoreCase("give")) {
				for (Player player : plugin.getServer().getOnlinePlayers()) {
					returnList.add(player.getName());
				}
			}
			// if subcommand is 'set', 'unset', 'bind' or 'delete', return list of destination keys
			else if (args[0].equalsIgnoreCase("set")
					|| args[0].equalsIgnoreCase("unset")
					|| args[0].equalsIgnoreCase("bind")
					|| args[0].equalsIgnoreCase("delete")) {
				returnList.addAll(plugin.dataStore.selectAllKeys());
			}
		}

		// if third argument
		if (args.length == 3) {

			// if subcommand is 'give', return list of destination keys
			if (args[0].equals("give")) {
				returnList.addAll(plugin.dataStore.selectAllKeys());
			}
		}

		return returnList;
	}


	/**
	 * command executor method for LodeStar
	 */
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
							 final String label, final String[] args) {

		String subcommand;

		// get subcommand
		if (args.length > 0) {
			subcommand = args[0];
		}
		// if no arguments, display usage for all commands
		else {
			displayUsage(sender, "all");
			return true;
		}

		// status command
		if (subcommand.equalsIgnoreCase("status")) {
			return statusCommand(sender);
		}

		// reload command
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender, args);
		}

		// give command
		if (subcommand.equalsIgnoreCase("give")) {
			return giveCommand(sender, args);
		}

		// destroy command
		if (subcommand.equalsIgnoreCase("destroy")) {
			return destroyCommand(sender, args);
		}

		//set command
		if (subcommand.equalsIgnoreCase("set")) {
			return setCommand(sender, args);
		}

		// delete command
		if (subcommand.equalsIgnoreCase("delete") || subcommand.equalsIgnoreCase("unset")) {
			return deleteCommand(sender, args);
		}

		// bind command
		if (subcommand.equalsIgnoreCase("bind")) {
			return bindCommand(sender, args);
		}

		// list command
		if (subcommand.equalsIgnoreCase("list")) {
			return listCommand(sender, args);
		}

		// help command
		if (subcommand.equalsIgnoreCase("help")) {
			return helpCommand(sender, args);
		}

		// send invalid command message
		Message.create(sender, COMMAND_FAIL_INVALID_COMMAND).send();
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		displayUsage(sender, "help");
		return true;
	}


	/**
	 * Display plugin settings
	 *
	 * @param sender the command sender
	 * @return {@code true} if command was successful, {@code false} to display usage
	 */
	private boolean statusCommand(final CommandSender sender) {

		// if command sender does not have permission to view status, output error message and return true
		if (!sender.hasPermission("lodestar.status")) {
			Message.create(sender, PERMISSION_DENIED_STATUS).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// output config settings
		String versionString = plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Storage type: "
				+ ChatColor.RESET + plugin.dataStore.getName());

		sender.sendMessage(ChatColor.GREEN + "Default material: "
				+ ChatColor.RESET + plugin.getConfig().getString("default-material"));

		sender.sendMessage(ChatColor.GREEN + "Minimum distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("minimum-distance"));

		sender.sendMessage(ChatColor.GREEN + "Warmup: "
				+ ChatColor.RESET
				+ languageManager.getTimeString(TimeUnit.SECONDS.toMillis(
				plugin.getConfig().getInt("teleport-warmup"))));

		sender.sendMessage(ChatColor.GREEN + "Cooldown: "
				+ ChatColor.RESET
				+ languageManager.getTimeString(TimeUnit.SECONDS.toMillis(
				plugin.getConfig().getInt("teleport-cooldown"))));

		sender.sendMessage(ChatColor.GREEN + "Shift-click required: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("shift-click"));

		sender.sendMessage(ChatColor.GREEN + "Cancel on damage/movement/interaction: "
				+ ChatColor.RESET + "[ "
				+ plugin.getConfig().getBoolean("cancel-on-damage") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-movement") + "/"
				+ plugin.getConfig().getBoolean("cancel-on-interaction") + " ]");

		sender.sendMessage(ChatColor.GREEN + "Remove from inventory: "
				+ ChatColor.RESET + plugin.getConfig().getString("remove-from-inventory"));

		sender.sendMessage(ChatColor.GREEN + "Allow in recipes: " + ChatColor.RESET
				+ plugin.getConfig().getBoolean("allow-in-recipes"));

		sender.sendMessage(ChatColor.GREEN + "From nether: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-nether"));

		sender.sendMessage(ChatColor.GREEN + "From end: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("from-end"));

		sender.sendMessage(ChatColor.GREEN + "Lightning: "
				+ ChatColor.RESET + plugin.getConfig().getBoolean("lightning"));

		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());

		return true;
	}


	/**
	 * Reload plugin settings
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean reloadCommand(final CommandSender sender, final String[] args) {

		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission("lodestar.reload")) {
			Message.create(sender, PERMISSION_DENIED_RELOAD).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String subcmd = args[0];

		// argument limits
		int maxArgs = 1;

		// check max arguments
		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender, subcmd);
			return true;
		}

		// reinstall main configuration if necessary
		plugin.saveDefaultConfig();

		// reload main configuration
		plugin.reloadConfig();

		// update enabledWorlds list
		plugin.worldManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload messages
		LanguageManager.reload();

		// reload datastore
		DataStore.reload();

		// set debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");

		// send reloaded message
		Message.create(sender, COMMAND_SUCCESS_RELOAD).send();
		return true;
	}


	/**
	 * Destroy a LodeStar item in hand
	 *
	 * @param sender the command sender
	 * @param args   command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean destroyCommand(final CommandSender sender, final String[] args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check that sender has permission
		if (!sender.hasPermission("lodestar.destroy")) {
			Message.create(sender, PERMISSION_DENIED_DESTROY).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String subcmd = args[0];

		// argument limits
		int maxArgs = 1;

		// if too many arguments, send error and usage message
		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		Player player = (Player) sender;
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// check that player is holding a LodeStar item
		if (!LodeStar.isItem(playerItem)) {
			Message.create(sender, COMMAND_FAIL_INVALID_ITEM).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}
		int quantity = playerItem.getAmount();
		String destinationName = LodeStar.getDestinationName(playerItem);
		playerItem.setAmount(0);
		player.getInventory().setItemInMainHand(playerItem);
		Message.create(sender, COMMAND_SUCCESS_DESTROY)
				.setMacro(ITEM_QUANTITY, quantity)
				.setMacro(DESTINATION, destinationName)
				.send();
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_DESTROY);
		return true;
	}


	/**
	 * Set attributes on LodeStar item
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean setCommand(final CommandSender sender, final String[] args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check for permission
		if (!sender.hasPermission("lodestar.set")) {
			Message.create(sender, PERMISSION_DENIED_SET).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.length < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		Player player = (Player) sender;
		Location location = player.getLocation();

		// set destinationName to passed argument
		String destinationName = String.join(" ", arguments);

		// check if destination name is a reserved name
		if (Destination.isReserved(destinationName)) {
			Message.create(sender, COMMAND_FAIL_SET_RESERVED)
					.setMacro(DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check if destination name exists and if so if player has overwrite permission
		Destination destination = plugin.dataStore.selectRecord(destinationName);

		// check for overwrite permission if destination already exists
		if (destination != null && sender.hasPermission("lodestar.set.overwrite")) {
			Message.create(sender, PERMISSION_DENIED_OVERWRITE)
					.setMacro(DESTINATION, destinationName)
					.send();

			// play sound effect
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// send warning message if name begins with a number
		if (Destination.deriveKey(destinationName).matches("^\\d*_.*")) {
			Message.create(sender, COMMAND_WARN_SET_NUMERIC_PREFIX)
					.setMacro(DESTINATION, destinationName)
					.send();
		}

		// create destination object
		destination = new Destination(destinationName, location);

		// store destination object
		plugin.dataStore.insertRecord(destination);

		// send success message to player
		Message.create(sender, COMMAND_SUCCESS_SET).setMacro(DESTINATION, destinationName).send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);
		return true;
	}


	/**
	 * Remove named destination
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean deleteCommand(final CommandSender sender, final String[] args) {

		// check for permission
		if (!sender.hasPermission("lodestar.delete")) {
			Message.create(sender, PERMISSION_DENIED_DELETE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}
		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check min arguments
		if (args.length < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender, subcmd);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String destinationName = String.join(" ", arguments);
		String key = Destination.deriveKey(destinationName);

		// test that destination name is not reserved name
		if (Destination.isReserved(destinationName)) {
			Message.create(sender, COMMAND_FAIL_DELETE_RESERVED)
					.setMacro(DESTINATION, destinationName)
					.send();

			// play sound effect
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// test that destination name is valid
		if (!Destination.exists(destinationName)) {
			Message.create(sender, COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// remove destination record from storage
		plugin.dataStore.deleteRecord(key);

		// send success message to player
		Message.create(sender, COMMAND_SUCCESS_DELETE)
				.setMacro(DESTINATION, destinationName)
				.send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
		return true;
	}


	/**
	 * Bind item in hand to destination
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean bindCommand(final CommandSender sender, final String[] args) {

		// command sender must be player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check sender has permission
		if (!sender.hasPermission("lodestar.bind")) {
			Message.create(sender, PERMISSION_DENIED_BIND).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		int minArgs = 2;

		// check minimum arguments
		if (args.length < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		Player player = (Player) sender;
		String destinationName = String.join(" ", arguments);

		// test that destination exists
		if (!Destination.exists(destinationName)) {
			Message.create(sender, COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get player item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// if default-item-only configured true, check that item in hand has default material and data
		if (plugin.getConfig().getBoolean("default-material-only")
				&& !sender.hasPermission("lodestar.default-override")) {
			if (!LodeStar.isDefaultItem(playerItem)) {
				Message.create(sender, COMMAND_FAIL_INVALID_ITEM)
						.setMacro(DESTINATION, destinationName)
						.send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// try to get formatted destination name from storage
		Destination destination = plugin.dataStore.selectRecord(destinationName);
		if (destination != null) {
			destinationName = destination.getDisplayName();
		}

		// set destination in item lore
		LodeStar.setMetaData(playerItem, destinationName);

		// send success message
		Message.create(sender, COMMAND_SUCCESS_BIND).setMacro(DESTINATION, destinationName).send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_BIND);
		return true;
	}


	/**
	 * List LodeStar destination names
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean listCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to list destinations, output error message and return true
		if (!sender.hasPermission("lodestar.list")) {
			Message.create(sender, PERMISSION_DENIED_LIST).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String subcmd = args[0];
		// argument limits
		int maxArgs = 2;

		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender, subcmd);
			return true;
		}

		int page = 1;

		if (args.length == 2) {
			try {
				page = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e) {
				// second argument not a page number, let default of 1 stand
			}
		}
		page = Math.max(1, page);

		int itemsPerPage = plugin.getConfig().getInt("list-page-size");

		// get all records from datastore
		final List<String> allKeys = plugin.dataStore.selectAllKeys();

		if (plugin.debug) {
			plugin.getLogger().info("Total records fetched from db: " + allKeys.size());
		}

		// if display list is empty, output list empty message and return
		if (allKeys.isEmpty()) {
			Message.create(sender, LIST_EMPTY).send();
			return true;
		}

		int pageCount = (allKeys.size() / itemsPerPage) + 1;
		if (page > pageCount) {
			page = pageCount;
		}
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), allKeys.size());

		List<String> displayKeys = allKeys.subList(startIndex, endIndex);

		// display list header
		Message.create(sender, LIST_HEADER).setMacro(PAGE_NUMBER, page).setMacro(PAGE_TOTAL, pageCount).send();

		int itemNumber = startIndex;

		for (String key : displayKeys) {

			// increment item number
			itemNumber++;

			Destination destination = plugin.dataStore.selectRecord(key);

			Message.create(sender, LIST_ITEM)
					.setMacro(DESTINATION, destination.getDisplayName())
					.setMacro(ITEM_NUMBER, itemNumber)
					.setMacro(LOCATION, destination.getLocation())
					.send();
		}

		// display list footer
		Message.create(sender, LIST_FOOTER).setMacro(PAGE_NUMBER, page).setMacro(PAGE_TOTAL, pageCount).send();

		return true;
	}


	/**
	 * Display help message for commands
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean helpCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("lodestar.help")) {
			Message.create(sender, PERMISSION_DENIED_HELP).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
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
		displayUsage(sender, command);
		return true;
	}


	/**
	 * Give a LodeStar item to a player
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean giveCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to give LodeStars, output error message and return true
		if (!sender.hasPermission("lodestar.give")) {
			Message.create(sender, PERMISSION_DENIED_GIVE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// convert args list to ArrayList so we can remove elements as we parse them
		List<String> arguments = new ArrayList<>(Arrays.asList(args));

		// get subcmd from arguments ArrayList
		String subcmd = arguments.get(0);

		// remove subcmd from ArrayList
		arguments.remove(0);

		// argument limits
		int minArgs = 2;

		// if too few arguments, send error and usage message
		if (args.length < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
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
				Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
				displayUsage(sender, subcmd);
				return true;
			}

			Player player = (Player) sender;
			ItemStack playerItem = player.getInventory().getItemInMainHand().clone();

			// if item in hand is a LodeStar item, set destination and material from item
			if (LodeStar.isItem(playerItem)) {

				destinationName = LodeStar.getDestinationName(playerItem);
				material = playerItem.getType();
			}
		}

		// try to parse all remaining arguments as destinationName
		if (!arguments.isEmpty()) {

			String testName = String.join(" ", arguments);

			// if resulting name is existing destination, set to destinationName
			if (Destination.exists(testName)) {
				destinationName = testName;

				// set arguments to empty list
				arguments.clear();
			}
		}

		// try to parse next argument as material
		if (!arguments.isEmpty()) {

			// try to match material
			material = Material.matchMaterial(arguments.get(0));

			// if material matched, remove argument from list
			if (material != null) {
				arguments.remove(0);
			}
		}

		// try to parse all remaining arguments as destinationName
		if (!arguments.isEmpty()) {
			String testName = String.join(" ", arguments);

			// if resulting name is valid destination, set to destinationName
			if (Destination.exists(testName)) {
				destinationName = testName;

				// set arguments to empty list
				arguments.clear();
			}
			// else given destination is invalid (but not blank), so send error message
			else {
				Message.create(sender, COMMAND_FAIL_INVALID_DESTINATION)
						.setMacro(DESTINATION, testName)
						.send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// if no destination name set, set destination to spawn
		if (destinationName.isEmpty()) {
			destinationName = "spawn";
		}

		// if no material set or default-material-only configured true, try to parse material from config
		if (material == null || plugin.getConfig().getBoolean("default-material-only")) {
			material = Material.matchMaterial(Objects.requireNonNull(plugin.getConfig().getString("default-material")));
		}

		// if still no material match, set to nether star
		if (material == null) {
			material = Material.NETHER_STAR;
		}

		// create item stack with material, quantity and data
		ItemStack itemStack = new ItemStack(material, quantity);

		// set item metadata on item stack
		LodeStar.setMetaData(itemStack, destinationName);

		// give item stack to target player
		giveItem(sender, targetPlayer, itemStack);

		return true;
	}


	/**
	 * Helper method for give command
	 *
	 * @param giver        the player issuing the command
	 * @param targetPlayer the player being given item
	 * @param itemStack    the LodeStar item being given
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	@SuppressWarnings("UnusedReturnValue")
	private boolean giveItem(final CommandSender giver, final Player targetPlayer, final ItemStack itemStack) {

		String key = LodeStar.getKey(itemStack);
		int quantity = itemStack.getAmount();
		int maxGiveAmount = plugin.getConfig().getInt("max-give-amount");

		// check quantity against configured max give amount
		if (maxGiveAmount >= 0) {
			quantity = Math.min(maxGiveAmount, quantity);
			itemStack.setAmount(quantity);
		}

		// test that item is a LodeStar item
		if (!LodeStar.isItem(itemStack)) {
			Message.create(giver, COMMAND_FAIL_INVALID_ITEM).send();
			plugin.soundConfig.playSound(giver, SoundId.COMMAND_FAIL);
			return true;
		}

		// add specified quantity of LodeStars to player inventory
		HashMap<Integer, ItemStack> noFit = targetPlayer.getInventory().addItem(itemStack);

		// count items that didn't fit in inventory
		int noFitCount = 0;
		for (int index : noFit.keySet()) {
			noFitCount += noFit.get(index).getAmount();
		}

		// if remaining items equals quantity given, send player-inventory-full message and return
		if (noFitCount == quantity) {
			Message.create(giver, COMMAND_FAIL_GIVE_INVENTORY_FULL)
					.setMacro(ITEM_QUANTITY, quantity)
					.send();
			return false;
		}

		// subtract noFitCount from quantity
		quantity = quantity - noFitCount;

		// get destination display name
		String destinationName = Destination.getName(key);

		// don't display messages if giving item to self
		if (!giver.getName().equals(targetPlayer.getName())) {

			// send message and play sound to giver
			Message.create(giver, COMMAND_SUCCESS_GIVE)
					.setMacro(DESTINATION, destinationName)
					.setMacro(ITEM_QUANTITY, quantity)
					.setMacro(TARGET_PLAYER, targetPlayer)
					.send();

			// if giver is in game, play sound
			if (giver instanceof Player) {
				plugin.soundConfig.playSound(giver, SoundId.COMMAND_SUCCESS_GIVE_SENDER);
			}

			// send message to target player
			Message.create(targetPlayer, COMMAND_SUCCESS_GIVE_TARGET)
					.setMacro(DESTINATION, destinationName)
					.setMacro(ITEM_QUANTITY, quantity)
					.setMacro(TARGET_PLAYER, giver)
					.send();
		}

		// play sound to target player
		plugin.soundConfig.playSound(targetPlayer, SoundId.COMMAND_SUCCESS_GIVE_TARGET);
		return true;
	}


	/**
	 * Match a player name to player object
	 *
	 * @param sender           the player issuing the command
	 * @param targetPlayerName the player name to match
	 * @return the matched player object, or null if no match
	 */
	private Player matchPlayer(final CommandSender sender, final String targetPlayerName) {

		Player targetPlayer;

		// check exact match first
		targetPlayer = plugin.getServer().getPlayer(targetPlayerName);

		// if no match, try substring match
		if (targetPlayer == null) {
			List<Player> playerList = plugin.getServer().matchPlayer(targetPlayerName);

			// if only one matching player, use it, otherwise send error message (no match or more than 1 match)
			if (playerList.size() == 1) {
				targetPlayer = playerList.get(0);
			}
		}

		// if match found, return target player object
		if (targetPlayer != null) {
			return targetPlayer;
		}

		// check if name matches known offline player
		HashSet<OfflinePlayer> matchedPlayers = new HashSet<>();
		for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
			if (targetPlayerName.equalsIgnoreCase(offlinePlayer.getName())) {
				matchedPlayers.add(offlinePlayer);
			}
		}
		if (matchedPlayers.isEmpty()) {
			Message.create(sender, COMMAND_FAIL_PLAYER_NOT_FOUND).send();
		}
		else {
			Message.create(sender, COMMAND_FAIL_PLAYER_NOT_ONLINE).send();
		}
		return null;
	}


	/**
	 * Display command usage
	 *
	 * @param sender  the command sender
	 * @param command the command for which to display usage string
	 */
	private void displayUsage(final CommandSender sender, String command) {

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
				sender.sendMessage(usageColor + "/lodestar give <player> [quantity] [material] [destination_name]");
			}
		}
	}

}
