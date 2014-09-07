package com.gipsyking.EnderCrates;

import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NMSWrapper {

	public static void restore(Block block, ItemStack itemStack) {
		net.minecraft.server.v1_7_R1.ItemStack nmsHandStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound tag = nmsHandStack.getTag();
		if (tag == null || !tag.hasKey("contents")) {
			return;
		}
		
		nmsHandStack.setTag(null);
		
		Chest chest = (Chest) block.getState();
		
		NBTTagList contents = tag.getList("contents", 10);
		
		Inventory inventory = chest.getInventory();
		for (int i = 0; i < contents.size(); i++) {
			NBTTagCompound itemTag = contents.get(i);
			
			net.minecraft.server.v1_7_R1.ItemStack nmsStack = net.minecraft.server.v1_7_R1.ItemStack.createStack(itemTag);

			inventory.addItem(CraftItemStack.asCraftMirror(nmsStack));
		}
		
		// try to get rid of custom name, but this doesnt work
		//TileEntityChest tileEntityChest = (TileEntityChest) ((CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
		//tileEntityChest.a("");
		
	}
	
	public static void unzip(ItemStack itemStack, Inventory inventory) {
		net.minecraft.server.v1_7_R1.ItemStack nmsHandStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound tag = nmsHandStack.getTag();
		if (tag == null || !tag.hasKey("contents")) {
			return;
		}
		
		nmsHandStack.setTag(null);
		
		NBTTagList contents = tag.getList("contents", 10);
		
		for (int i = 0; i < contents.size(); i++) {
			NBTTagCompound itemTag = contents.get(i);
			
			net.minecraft.server.v1_7_R1.ItemStack nmsStack = net.minecraft.server.v1_7_R1.ItemStack.createStack(itemTag);

			inventory.addItem(CraftItemStack.asCraftMirror(nmsStack));
		}
		
	}

	public static ItemStack crateItem(Inventory inventory) {
		return crateItem(inventory.getContents());
	}
	
	public static ItemStack crateItem(ItemStack[] stacks) {
		net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(new ItemStack(Material.CHEST));
		NBTTagCompound tag = new NBTTagCompound();
		nmsStack.setTag(tag);
		
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", "Crate");
		NBTTagList lore = new NBTTagList();
		
		NBTTagList contents = new NBTTagList();
		
		for (ItemStack invStack: stacks) {
			if (invStack == null) {
				continue;
			}
			lore.add(new NBTTagString(invStack.getType().toString() + " (" + invStack.getAmount() + ")"));
			
			
			NBTTagCompound itemTag = new NBTTagCompound();
			CraftItemStack.asNMSCopy(invStack).save(itemTag);
						
			contents.add(itemTag);
		}
		display.set("Lore", lore);
		
		tag.set("display", display);
		tag.set("contents", contents);
		
		return CraftItemStack.asCraftMirror(nmsStack);
	}

	public static ItemStack emptyCrateItem() {
		net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_CHEST));
		NBTTagCompound tag = new NBTTagCompound();
		nmsStack.setTag(tag);
		
		NBTTagCompound display = new NBTTagCompound();
		tag.set("display", display);
		display.setString("Name", "Crate");
		NBTTagList lore = new NBTTagList();
		display.set("Lore", lore);
		
		return CraftItemStack.asCraftMirror(nmsStack);
	}
}
