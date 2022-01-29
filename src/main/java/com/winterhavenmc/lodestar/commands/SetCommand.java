package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.PluginMain;
import com.winterhavenmc.lodestar.sounds.SoundId;
import com.winterhavenmc.lodestar.storage.Destination;

import com.winterhavenmc.lodestar.messages.Macro;
import com.winterhavenmc.lodestar.messages.MessageId;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


final class SetCommand extends SubcommandAbstract {

	private final PluginMain plugin;


	SetCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "set";
		this.usageString = "/lodestar set <destination_name>";
		this.description = MessageId.COMMAND_HELP_SET;
		this.minArgs = 1;
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
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check for permission
		if (!sender.hasPermission("lodestar.set")) {
			plugin.messageBuilder.build(sender, MessageId.PERMISSION_DENIED_SET).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs()) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
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
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_SET_RESERVED)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check if destination name exists and if so, check if player has overwrite permission
		Destination destination = plugin.dataStore.selectRecord(destinationName);

		// check for overwrite permission if destination already exists
		if (destination != null && sender.hasPermission("lodestar.set.overwrite")) {
			plugin.messageBuilder.build(sender, MessageId.PERMISSION_DENIED_OVERWRITE)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();

			// play sound effect
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// send warning message if name begins with a number
		if (Destination.deriveKey(destinationName).matches("^\\d*_.*")) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_WARN_SET_NUMERIC_PREFIX)
					.setMacro(Macro.DESTINATION, destinationName)
					.send();
		}

		// create destination object
		destination = new Destination(destinationName, location);

		// store destination object
		plugin.dataStore.insertRecord(destination);

		// send success message to player
		plugin.messageBuilder.build(sender, MessageId.COMMAND_SUCCESS_SET).setMacro(Macro.DESTINATION, destinationName).send();

		// play sound effect
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_SUCCESS_SET);

		return true;
	}

}
