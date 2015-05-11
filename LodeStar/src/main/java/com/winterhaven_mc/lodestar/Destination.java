package com.winterhaven_mc.lodestar;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Destination {
	
	private static final LodeStarMain plugin = LodeStarMain.instance;

	private String key;
	private String displayName;
	private Location location;
	
	// class constructor
	Destination(String displayName, Location location) {
		this.setKey(displayName);
		this.setDisplayName(displayName);
		this.setLocation(location);
	}
	
	// class constructor
	Destination(String key, String displayName, Location location) {
		this.setKey(key);
		this.setDisplayName(displayName);
		this.setLocation(location);
	}
	
	public boolean isSpawn() {
		if (this.getKey().equals("spawn") 
				|| this.getKey().equals(deriveKey(plugin.messageManager.getSpawnDisplayName()))) {
			return true;
		}
		return false;
	}
	
	public boolean isHome() {
		if (this.getKey().equals("home") 
				|| this.getKey().equals(deriveKey(plugin.messageManager.getHomeDisplayName()))) {
			return true;
		}
		return false;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = deriveKey(key);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName.replace('_', ' ');
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public static String deriveKey(String key) {
		key = key.replace(' ', '_');
		key = key.toLowerCase().replaceAll("[&" + ChatColor.COLOR_CHAR + "][0-9a-zA-Zk-oK-OrR]", "");
		return key;
	}
}
