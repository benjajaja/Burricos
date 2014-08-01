package com.gipsyking.EnderCrates;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;


public class CrateInventory {
	public Inventory inventory;
	public Block block;
	public Player player;
	public InventoryView view;

	public CrateInventory(Inventory inventory, InventoryView view, Block clickedBlock, Player player) {
		this.inventory = inventory;
		this.view = view;
		this.block = clickedBlock;
		this.player = player;
	}
}
