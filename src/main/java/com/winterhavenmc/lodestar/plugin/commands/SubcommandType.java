/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.lodestar.plugin.commands;

import com.winterhavenmc.lodestar.plugin.PluginMain;


enum SubcommandType
{
	BIND()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new BindSubcommand(plugin);
				}
			},

	DELETE()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new DeleteSubcommand(plugin);
				}
			},

	DESTROY()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new DestroySubcommand(plugin);
				}
			},

	GIVE()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new GiveSubcommand(plugin);
				}
			},

	LIST()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new ListSubcommand(plugin);
				}
			},

	RELOAD()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new ReloadSubcommand(plugin);
				}
			},

	SET()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new SetSubcommand(plugin);
				}
			},

	STATUS()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new StatusSubcommand(plugin);
				}
			},

	TELEPORT()
			{
				@Override
				Subcommand create(final PluginMain plugin)
				{
					return new TeleportSubcommand(plugin);
				}
			};


	abstract Subcommand create(final PluginMain plugin);

}
