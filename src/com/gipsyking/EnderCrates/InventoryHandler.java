package com.gipsyking.EnderCrates;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryHandler {

	private ArrayList<CrateInventory> inventories = new ArrayList<CrateInventory>();
	private EnderCrates plugin;
	public Logger logger;
	
	public InventoryHandler(EnderCrates enderplugin) {
		this.plugin = enderplugin;
		this.logger = Logger.getLogger("EnderCrates");
	}
	
	public void openInventory(Player player, Block block) {
		Inventory inventory = plugin.getServer().createInventory(player, InventoryType.ENDER_CHEST);
		InventoryView view = player.openInventory(inventory);
		inventories.add(new CrateInventory(inventory, view, block, player));
	}

	public void saveInventory(InventoryView inventoryView) {
		CrateInventory crate = getCrate(inventoryView);
		if (crate != null) {
			for (ItemStack stack: crate.inventory.getContents()) {
				if (stack != null) {
					drop(crate);
					return;
				}
			}
			dropNew(crate);
		}
		
	}

	private CrateInventory getCrate(InventoryView view) {
		if (view.getType() != InventoryType.ENDER_CHEST) {
			return null;
		}
		for (CrateInventory crate: inventories) {
			if (crate.view.equals(view)) {
				inventories.remove(crate);
				return crate;
			}
		}
		return null;
	}
	
	public void restore(Block block, ItemStack itemStack) {
		net.minecraft.server.v1_7_R1.ItemStack nmsHandStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound tag = nmsHandStack.getTag();
		if (tag == null) {
			return;
		}
		
		if (!tag.hasKey("contents")) {
			logger.info("no contents: " + tag);
			return;
		}
		
		nmsHandStack.setTag(null);
		
		block.setType(Material.CHEST);
		
		NBTTagList contents = tag.getList("contents", 10);
		
		Inventory inventory = ((Chest) block.getState()).getBlockInventory();
		for (int i = 0; i < contents.size(); i++) {
			NBTTagCompound itemTag = contents.get(i);
			
			@SuppressWarnings("deprecation")
			ItemStack stack = new ItemStack(itemTag.getInt("type"));
			
			stack.setDurability(itemTag.getShort("durability"));
			stack.setAmount(itemTag.getInt("amount"));
			
			net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
			if (itemTag.hasKey("nbt")) {
				logger.info("restoring tag: " + itemTag.getCompound("nbt"));
				nmsStack.setTag(itemTag.getCompound("nbt"));
			}

			inventory.addItem(CraftItemStack.asCraftMirror(nmsStack));
		}
	}
	
	private void dropCrate(CrateInventory crate, ItemStack stack) {
		crate.block.getWorld().dropItemNaturally(crate.block.getLocation(), stack);
		crate.block.setType(Material.AIR);
	}
	
	public void drop(CrateInventory crate) {
		net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_CHEST));
		NBTTagCompound tag = new NBTTagCompound();
		nmsStack.setTag(tag);
		
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", "Crate");
		NBTTagList lore = new NBTTagList();
		
		NBTTagList contents = new NBTTagList();
		
		for (ItemStack invStack: crate.inventory.getContents()) {
			if (invStack == null) {
				continue;
			}
			lore.add(new NBTTagString(invStack.getType().toString() + " (" + invStack.getAmount() + ")"));
			
			
//			CraftItemStack.asNMSCopy(invStack).save(nbttagcompound)
			NBTTagCompound itemTag = new NBTTagCompound();
			
			itemTag.setInt("type", invStack.getTypeId());
			itemTag.setShort("durability", invStack.getDurability());
			itemTag.setInt("amount", invStack.getAmount());
			
			NBTTagCompound itemTagData = CraftItemStack.asNMSCopy(invStack).tag;
			if (itemTagData != null) {
				itemTag.set("nbt", itemTagData.clone());
			}
			
			contents.add(itemTag);
		}
		display.set("Lore", lore);
		
		tag.set("display", display);
		tag.set("contents", contents);
		
		logger.info("tag: " + tag);
		
		dropCrate(crate, CraftItemStack.asCraftMirror(nmsStack));
	}

	public void dropNew(CrateInventory crate) {
		net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_CHEST));
		NBTTagCompound tag = new NBTTagCompound();
		nmsStack.setTag(tag);
		
		NBTTagCompound display = new NBTTagCompound();
		tag.set("display", display);
		display.setString("Name", "Crate");
		NBTTagList lore = new NBTTagList();
		display.set("Lore", lore);
		
		dropCrate(crate, CraftItemStack.asCraftMirror(nmsStack));
		
	}
}
