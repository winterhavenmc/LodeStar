package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.messages.Message;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import com.winterhaven_mc.lodestar.storage.Destination;
import com.winterhaven_mc.lodestar.util.LodeStar;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.winterhaven_mc.lodestar.messages.Macro.DESTINATION;
import static com.winterhaven_mc.lodestar.messages.MessageId.*;


public class BindCommand extends AbstractCommand {

	private final PluginMain plugin;

	private final List<Material> invalidMaterials = new ArrayList<>(Arrays.asList(
				Material.AIR,
				Material.VOID_AIR,
				Material.VOID_AIR
		));


	BindCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("bind");
		this.setUsage("/lodestar bind <destination name>");
		this.setDescription(COMMAND_HELP_BIND);
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

		// check minimum arguments
		if (args.size() < getMinArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// cast sender to player
		Player player = (Player) sender;

		// join remaining arguments into destination name
		String destinationName = String.join(" ", args);

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
				Message.create(sender, COMMAND_FAIL_INVALID_MATERIAL)
						.setMacro(DESTINATION, destinationName)
						.send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// check that item in hand is valid material
		if (invalidMaterials.contains(playerItem.getType())) {
			Message.create(sender, COMMAND_FAIL_INVALID_MATERIAL)
					.setMacro(DESTINATION, destinationName)
					.send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
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

}
