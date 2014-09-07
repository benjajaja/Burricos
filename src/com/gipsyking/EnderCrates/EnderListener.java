package com.gipsyking.EnderCrates;

import java.util.logging.Logger;

import net.minecraft.server.v1_7_R1.EntityHorse;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.InventoryHorseChest;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventoryHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

public class EnderListener implements Listener{

	private InventoryHandler ih;
	
	public EnderListener(InventoryHandler ih){
		this.ih = ih;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void playerInteractEvent(PlayerInteractEvent event){
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST
				|| event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		event.setCancelled(true);
		ih.openInventory(event.getPlayer(), event.getClickedBlock());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void inventoryOpenEvent(InventoryOpenEvent event) {
		if (event.getInventory() instanceof CraftInventoryHorse &&
				event.getPlayer().getItemInHand().getType() != Material.ENDER_CHEST) {
			EntityHorse horse = ((CraftHorse) event.getInventory().getHolder()).getHandle();
			if (!horse.hasChest()) {
				return;
			}
			
			event.setCancelled(true);
			
			EntityPlayer h = ((CraftPlayer)event.getPlayer()).getHandle();
			h.openContainer(horse.inventoryChest);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void setChestEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Horse) || event.getPlayer().getItemInHand().getType() != Material.ENDER_CHEST) {
			return;
		}
		EntityHorse horse = ((CraftHorse) event.getRightClicked()).getHandle();
		if (!horse.hasChest()) {
			horse.setHasChest(true);
			horse.inventoryChest = new InventoryHorseChest("Burrico", 54, horse);
			event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoaded(ChunkLoadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				EntityHorse horse = ((CraftHorse) entity).getHandle();
				if (horse.hasChest()) {
					NBTTagCompound nbttagcompound = new NBTTagCompound();
					horse.b(nbttagcompound);
					Logger.getGlobal().info("tag: " + nbttagcompound);
					
					ItemStack crate = ((CraftHorse) entity).getInventory().getItem(2);
					
					horse.inventoryChest = new InventoryHorseChest("Burrico", 54, horse);
					
					NMSWrapper.unzip(crate, ((CraftHorse) entity).getInventory());
					
//					NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);
//					for (int i = 0; i < nbttaglist.size(); ++i) {
//		                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
//		                int j = nbttagcompound1.getByte("Slot") & 255;
//
//		                if (j >= 2 && j < horse.inventoryChest.getSize()) {
//		                	horse.inventoryChest.setItem(j, net.minecraft.server.v1_7_R1.ItemStack.createStack(nbttagcompound1));
//		                }
//		            }
//					horse.a(tag);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				CraftHorse horse = (CraftHorse) entity;
//				EntityHorse horse = ((CraftHorse) entity).getHandle();
				if (horse.isCarryingChest()) {
					ItemStack crate = NMSWrapper.crateItem(((CraftHorse) entity).getInventory().getContents());
					horse.getInventory().clear();
					horse.getInventory().setItem(2, crate);
					
					EntityHorse nmsHorse = horse.getHandle();
					NBTTagCompound nbttagcompound = new NBTTagCompound();
					nmsHorse.b(nbttagcompound);
					Logger.getGlobal().info("tag: " + nbttagcompound);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void inventoryCloseEvent(InventoryCloseEvent event) {
		ih.saveInventory(event.getView());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void blockPlaceEvent(BlockPlaceEvent event) {
		if (event.isCancelled() || event.getBlock().getType() != Material.CHEST) return;
		
		ih.restore(event.getBlock(), event.getItemInHand());
	}
	
}
