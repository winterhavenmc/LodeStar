package com.winterhaven_mc.lodestar.commands;

import com.winterhaven_mc.lodestar.PluginMain;


public enum SubcommandType {

	BIND() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new BindCommand(plugin));
		}
	},

	DELETE() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new DeleteCommand(plugin));
		}
	},

	DESTROY() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new DestroyCommand(plugin));
		}
	},

	GIVE() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new GiveCommand(plugin));
		}
	},

	HELP() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new HelpCommand(plugin, subcommandMap));
		}
	},

	RELOAD() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new ReloadCommand(plugin));
		}
	},

	STATUS() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new StatusCommand(plugin));
		}
	},

	TELEPORT() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new TeleportCommand(plugin));
		}
	};


	abstract void register(final PluginMain plugin, final SubcommandMap subcommandMap);

}
