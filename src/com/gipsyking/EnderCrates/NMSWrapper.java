package com.gipsyking.EnderCrates;

import net.minecraft.server.v1_7_R1.EntityHorse;
import net.minecraft.server.v1_7_R1.InventoryHorseChest;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NMSWrapper {

	
	public static ItemStack crateItem(Inventory inventory) {
		return crateItem(inventory.getContents());
	}
	
	public static ItemStack crateItem(ItemStack[] stacks) {
		net.minecraft.server.v1_7_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(new ItemStack(Material.CHEST));
		NBTTagCompound tag = new NBTTagCompound();
		nmsStack.setTag(tag);
		
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", "Donkey.zip");
		NBTTagList lore = new NBTTagList();
		lore.add(new NBTTagString("Error - place in first slot and reload chunk..."));
		display.set("Lore", lore);
		
		NBTTagList contents = new NBTTagList();
		
		for (ItemStack invStack: stacks) {
			NBTTagCompound itemTag = new NBTTagCompound();
			if (invStack != null) {
				CraftItemStack.asNMSCopy(invStack).save(itemTag);
			}
						
			contents.add(itemTag);
		}
		
		tag.set("display", display);
		tag.set("contents", contents);
		
		return CraftItemStack.asCraftMirror(nmsStack);
	}

	public static void unzip(ItemStack itemStack, Inventory inventory) {
		net.minecraft.server.v1_7_R1.ItemStack nmsHandStack = CraftItemStack.asNMSCopy(itemStack);
		if (nmsHandStack == null) {
			return;
		}
		NBTTagCompound tag = nmsHandStack.getTag();
		if (tag == null || !tag.hasKey("contents")) {
			return;
		}
		
		nmsHandStack.setTag(null);
		
		NBTTagList contents = tag.getList("contents", 10);
		
		for (int i = 0; i < contents.size(); i++) {
			NBTTagCompound itemTag = contents.get(i);
			
			net.minecraft.server.v1_7_R1.ItemStack nmsStack = net.minecraft.server.v1_7_R1.ItemStack.createStack(itemTag);
			
			if (!itemTag.isEmpty()) {
				inventory.setItem(i, CraftItemStack.asCraftMirror(nmsStack));
			}
		}
		
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

	public static void openDonkeyContainer(Player player, Horse horse) {
		
		((CraftPlayer)player).getHandle().openContainer(((CraftHorse)horse).getHandle().inventoryChest);
	}

	public static void setDonkeyChest(Horse horse) {
		EntityHorse handle = ((CraftHorse)horse).getHandle();
		handle.inventoryChest = new InventoryHorseChest("Burrico", 54, handle);
	}

	public static void unsetDonkeyChest(Horse horse) {
		horse.getInventory().clear();
		
		EntityHorse handle = ((CraftHorse)horse).getHandle();
		handle.inventoryChest = null;
		handle.loadChest();
	}
}
