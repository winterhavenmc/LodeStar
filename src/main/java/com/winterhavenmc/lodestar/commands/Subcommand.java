package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;


interface Subcommand {

	boolean onCommand(final CommandSender sender, final List<String> argsList);

	List<String> onTabComplete(final CommandSender sender, final Command command,
							   final String alias, final String[] args);

	String getName();

	Collection<String> getAliases();

	void addAlias(final String alias);

	@SuppressWarnings("unused")
	String getUsage();

	void displayUsage(final CommandSender sender);

	MessageId getDescription();

	int getMinArgs();

	int getMaxArgs();

}
