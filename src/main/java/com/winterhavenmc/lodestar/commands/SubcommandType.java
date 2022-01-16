package com.winterhavenmc.lodestar.commands;

import com.winterhavenmc.lodestar.PluginMain;


public enum SubcommandType {

	BIND() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new BindCommand(plugin);
		}
	},

	DELETE() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new DeleteCommand(plugin);
		}
	},

	DESTROY() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new DestroyCommand(plugin);
		}
	},

	GIVE() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new GiveCommand(plugin);
		}
	},

	LIST() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ListCommand(plugin);
		}
	},

	RELOAD() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ReloadCommand(plugin);
		}
	},

	SET() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new SetCommand(plugin);
		}
	},

	STATUS() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new StatusCommand(plugin);
		}
	},

	TELEPORT() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new TeleportCommand(plugin);
		}
	};


	abstract Subcommand create(final PluginMain plugin);

}
