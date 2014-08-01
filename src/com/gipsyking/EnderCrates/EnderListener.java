package com.gipsyking.EnderCrates;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ReinforcementManager;

public class EnderListener implements Listener{

	private InventoryHandler ih;
	private ReinforcementManager rm;
	
	public EnderListener(InventoryHandler ih){
		this.ih = ih;
		rm = Citadel.getReinforcementManager();
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void playerInteractEvent(PlayerInteractEvent event){
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST
				|| event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ih.openInventory(event.getPlayer(), event.getClickedBlock());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void inventoryCloseEvent(InventoryCloseEvent event) {
		ih.saveInventory(event.getView());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void blockPlaceEvent(BlockPlaceEvent event) {
		if (event.getBlock().getType() != Material.ENDER_CHEST) return;
		
		ih.restore(event.getBlock(), event.getItemInHand());
	}
	
}
