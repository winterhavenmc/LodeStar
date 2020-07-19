package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.Macro.DESTINATION;
import static com.winterhaven_mc.lodestar.messages.MessageId.*;
import static com.winterhaven_mc.lodestar.messages.MessageId.COMMAND_FAIL_INVALID_DESTINATION;


public class TeleportCommand extends AbstractCommand {

	private final PluginMain plugin;


	TeleportCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("teleport");
		this.setUsage("/lodestar teleport <destination_name>");
		this.setDescription(COMMAND_HELP_TELEPORT);
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

		// check for permission
		if (!sender.hasPermission("lodestar.teleport")) {
			Message.create(sender, PERMISSION_DENIED_TELEPORT).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check for in game player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		Player player = (Player) sender;

		int minArgs = 2;

		// check min arguments
		if (args.size() < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// join remaining arguments to get destination name
		String destinationName = String.join(" ", args);

		// test that destination name is valid
		if (!Destination.exists(destinationName)) {
			Message.create(sender, COMMAND_FAIL_INVALID_DESTINATION)
					.setMacro(DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get destination from datastore
		Destination destination = plugin.dataStore.selectRecord(destinationName);

		if (destination != null && destination.getLocation() != null) {
			plugin.soundConfig.playSound(player.getLocation(), SoundId.TELEPORT_SUCCESS_DEPARTURE);
			player.teleport(destination.getLocation());
			Message.create(sender, TELEPORT_SUCCESS).setMacro(DESTINATION, destination).send();
			plugin.soundConfig.playSound(destination.getLocation(), SoundId.TELEPORT_SUCCESS_ARRIVAL);
			return true;
		}
		else {
			Message.create(sender, COMMAND_FAIL_INVALID_DESTINATION).send();
			plugin.soundConfig.playSound(sender, SoundId.TELEPORT_DENIED_WORLD_DISABLED);
		}

		return true;
	}

}
