package com.gipsyking.EnderCrates;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

public class EnderListener implements Listener{

	
	private static final int CRATE_SLOT = 2;

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void inventoryOpenEvent(InventoryOpenEvent event) {
		if (!(event.getInventory() instanceof HorseInventory)
				|| !((Horse)event.getInventory().getHolder()).isCarryingChest()) {
			return;
		}
		
		event.setCancelled(true);
		NMSWrapper.openDonkeyContainer((Player) event.getPlayer(), (Horse) event.getInventory().getHolder());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void setDonkeyDoubleChestEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Horse) || event.getPlayer().getItemInHand().getType() != Material.ENDER_CHEST) {
			return;
		}
		
		Horse horse = (Horse) event.getRightClicked();
		if (!horse.isCarryingChest()) {
			event.setCancelled(true);
			horse.setCarryingChest(true);
			event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			NMSWrapper.setDonkeyChest(horse);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoaded(ChunkLoadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest()) {
					ItemStack crate = horse.getInventory().getItem(CRATE_SLOT);
					NMSWrapper.setDonkeyChest(horse);
					NMSWrapper.unzip(crate, horse.getInventory());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest()) {
					this.saveDonkeyCrate(horse);
				}
			}
		}
	}

	public static void saveDonkeyCrate(Horse horse) {
		ItemStack crate = NMSWrapper.crateItem(horse.getInventory().getContents());
		NMSWrapper.unsetDonkeyChest(horse);
		horse.getInventory().setItem(CRATE_SLOT, crate);
		Logger.getGlobal().info("Donkey saved crate");
	}
	
}
