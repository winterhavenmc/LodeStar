package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.Macro.DESTINATION;
import static com.winterhaven_mc.lodestar.messages.MessageId.*;


public class DeleteCommand extends AbstractCommand {

	private final PluginMain plugin;


	DeleteCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("delete");
		this.setUsage("/lodestar delete <destination name>");
		this.addAlias("unset");
		this.setDescription(COMMAND_HELP_DELETE);
		this.setMinArgs(1);
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
		if (!sender.hasPermission("lodestar.delete")) {
			Message.create(sender, PERMISSION_DENIED_DELETE).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check min arguments
		if (args.size() < getMinArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// join remaining arguments into destination name
		String destinationName = String.join(" ", args);

		// get key for destination name
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

}
