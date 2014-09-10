package com.gipsyking.Burricos;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Burricos extends JavaPlugin implements Listener{
	
	static Logger logger;
	
	private static final int ZIP_SLOT = 2;
	public static final String ZIP_LORE = "Donkey.zip";

	public void onEnable(){
		Burricos.logger = getLogger();
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		int count = 0;
		for (World world: this.getServer().getWorlds()) {
			for (Horse horse: world.getEntitiesByClass(Horse.class)) {
				if (horse.isCarryingChest() && horse.getInventory().getSize() == 54) {
					Burricos.saveDonkeyZip(horse);
					count++;
				}
			}
		}
		logger.info("Zipped " + count + " donkey inventories");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void inventoryOpenEvent(InventoryOpenEvent event) {
		if (!(event.getInventory() instanceof HorseInventory)
				|| !((Horse)event.getInventory().getHolder()).isCarryingChest()) {
			return;
		}
		
		Horse horse = (Horse) event.getInventory().getHolder();
		
		if (horse.getInventory().getSize() != 54) {
			// it's not an upgraded donkey
			return;
		}
		
		event.setCancelled(true);
		NMSWrapper.openDonkeyContainer((Player) event.getPlayer(), horse);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void setDonkeyDoubleChestEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Horse) || event.getPlayer().getItemInHand().getType() != Material.CHEST) {
			return;
		}
		
		ItemStack chestItem = event.getPlayer().getItemInHand();
		if (chestItem.getItemMeta().getLore() == null
				|| chestItem.getItemMeta().getLore().size() != 1
				|| !chestItem.getItemMeta().getLore().get(0).equals("Donkey double chest")) {
			// must be used with a chest with exactly that lore, from a Factory or something
			return;
		}
		
		Horse horse = (Horse) event.getRightClicked();
		if (!horse.isCarryingChest()) {
			event.setCancelled(true);
			horse.setCarryingChest(true);
			event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			
			HorseInventory inventory = horse.getInventory();
			NMSWrapper.setLargeDonkeyChest(horse);
			for (int i = 0; i < inventory.getSize(); i++) {
				if (inventory.getItem(i) != null) {
					horse.getInventory().setItem(i, inventory.getItem(i).clone());
				}
			}
			inventory.clear(); // could another player be looking at the old inventory view?
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoaded(ChunkLoadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest()) {
					ItemStack zip = horse.getInventory().getItem(ZIP_SLOT);
					if (this.isZipItem(zip)) {
						NMSWrapper.setLargeDonkeyChest(horse);
						NMSWrapper.unzip(zip, horse.getInventory());
					}
				}
			}
		}
	}

	private boolean isZipItem(ItemStack zip) {
		return zip != null
				&& zip.getItemMeta() != null
				&& zip.getItemMeta().getLore() != null
				&& zip.getItemMeta().getLore().size() == 1
				&& zip.getItemMeta().getLore().get(0).equals(ZIP_LORE);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest() && horse.getInventory().getSize() == 54) {
					Burricos.saveDonkeyZip(horse);
				}
			}
		}
	}

	public static void saveDonkeyZip(Horse horse) {
		ItemStack crate = NMSWrapper.zip(horse.getInventory().getContents());
		NMSWrapper.unsetLargeDonkeyChest(horse);
		horse.getInventory().setItem(ZIP_SLOT, crate);
	}
}
