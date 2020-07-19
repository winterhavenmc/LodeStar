package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.Macro.DESTINATION;
import static com.winterhaven_mc.lodestar.messages.MessageId.*;


public class SetCommand extends AbstractCommand {

	private final PluginMain plugin;


	SetCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("set");
		this.setUsage("/lodestar set <destination_name>");
		this.setDescription(COMMAND_HELP_SET);
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		if (args.length == 2) {
			return plugin.dataStore.selectAllKeys();
		}

		return Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		int minArgs = 2;

		// check for permission
		if (!sender.hasPermission("lodestar.set")) {
			Message.create(sender, PERMISSION_DENIED_SET).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.size() < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		Player player = (Player) sender;
		Location location = player.getLocation();

		// set destinationName to passed argument
		String destinationName = String.join(" ", args);

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

}
