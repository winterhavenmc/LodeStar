package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.messages.MessageId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


abstract class SubcommandAbstract implements Subcommand {

	private String name;
	private Collection<String> aliases = new HashSet<>();
	private String usageString;
	private MessageId description;
	private int minArgs;
	private int maxArgs;


	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public Collection<String> getAliases() {
		return aliases;
	}

	@Override
	public void setAliases(final Collection<String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public void addAlias(final String alias) {
		this.aliases.add(alias);
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(final CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public void setUsage(final String usageString) {
		this.usageString = usageString;
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public void setDescription(final MessageId description) {
		this.description = description;
	}

	@Override
	public int getMinArgs() { return minArgs; }

	@Override
	public void setMinArgs(int minArgs) {
		this.minArgs = minArgs;
	}

	@Override
	public int getMaxArgs() { return maxArgs; }

	@Override
	public void setMaxArgs(int maxArgs) {
		this.maxArgs = maxArgs;
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		return Collections.emptyList();
	}

}
