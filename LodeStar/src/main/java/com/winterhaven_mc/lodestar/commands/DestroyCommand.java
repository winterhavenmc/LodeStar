package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;
import com.winterhaven_mc.lodestar.sounds.SoundId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.lodestar.messages.Macro.DESTINATION;
import static com.winterhaven_mc.lodestar.messages.Macro.ITEM_QUANTITY;
import static com.winterhaven_mc.lodestar.messages.MessageId.*;


public class DestroyCommand extends AbstractCommand {

	private final PluginMain plugin;


	DestroyCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("destroy");
		this.setUsage("/lodestar destroy");
		this.setDescription(COMMAND_HELP_DESTROY);
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// sender must be in game player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_CONSOLE).send(plugin.languageHandler);
			return true;
		}

		// check that sender has permission
		if (!sender.hasPermission("lodestar.destroy")) {
			plugin.messageBuilder.build(sender, PERMISSION_DENIED_DESTROY).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// cast sender to player
		Player player = (Player) sender;

		// get item in hand
		ItemStack playerItem = player.getInventory().getItemInMainHand();

		// check that item player is holding is a LodeStar item
		if (!plugin.lodeStarFactory.isItem(playerItem)) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_INVALID_ITEM).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		int quantity = playerItem.getAmount();
		String destinationName = plugin.lodeStarFactory.getDestinationName(playerItem);
		playerItem.setAmount(0);
		player.getInventory().setItemInMainHand(playerItem);
		plugin.messageBuilder.build(sender, COMMAND_SUCCESS_DESTROY)
				.setMacro(ITEM_QUANTITY, quantity)
				.setMacro(DESTINATION, destinationName)
				.send(plugin.languageHandler);
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_DESTROY);

		return true;
	}

}
