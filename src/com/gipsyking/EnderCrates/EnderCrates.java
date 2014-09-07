package com.gipsyking.EnderCrates;

import org.bukkit.World;
import org.bukkit.entity.Horse;
import org.bukkit.plugin.java.JavaPlugin;


public class EnderCrates extends JavaPlugin{
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(new EnderListener(), this);
	}
	
	public void onDisable() {
		for (World world: this.getServer().getWorlds()) {
			for (Horse horse: world.getEntitiesByClass(Horse.class)) {
				if (horse.isCarryingChest()) {
					EnderListener.saveDonkeyCrate(horse);
				}
			}
		}
	}
}
