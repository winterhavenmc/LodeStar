package com.winterhavenmc.lodestar.bootstrap;

import com.winterhavenmc.lodestar.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.lodestar.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;


public final class Bootstrap
{
	private Bootstrap() { /* private constructor to prevent instantiation */ }


	public static ConnectionProvider getConnectionProvider(Plugin plugin)
	{
		return new SqliteConnectionProvider(plugin);
	}

}
