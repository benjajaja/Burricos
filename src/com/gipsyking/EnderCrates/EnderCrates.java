package com.gipsyking.EnderCrates;

import org.bukkit.plugin.java.JavaPlugin;


public class EnderCrates extends JavaPlugin{
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(new EnderListener(new InventoryHandler(this)), this);
	}
}
