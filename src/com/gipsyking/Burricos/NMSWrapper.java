package com.gipsyking.Burricos;

// all the cancer, at least it's in one place
import net.minecraft.server.v1_7_R4.EntityHorse;
import net.minecraft.server.v1_7_R4.InventoryHorseChest;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagString;
import net.minecraft.server.v1_7_R4.ItemStack;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class NMSWrapper {

	/**
	 * "zip" an inventory array into the NBT of a single item. If item is null a new item will be created.
	 */
	public static org.bukkit.inventory.ItemStack zip(org.bukkit.inventory.ItemStack[] stacks, org.bukkit.inventory.ItemStack zipItem) {
		ItemStack nmsStack;
		NBTTagCompound tag;

		if (zipItem == null) {
			nmsStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.SADDLE));
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
			
			NBTTagCompound display = new NBTTagCompound();
			display.setString("Name", Burricos.ZIP_NAME);
			tag.set("display", display);
			
			NBTTagList lore = new NBTTagList();
			lore.add(new NBTTagString(Burricos.ZIP_LORE));
			display.set("Lore", lore);
			
		} else {
			nmsStack = CraftItemStack.asNMSCopy(zipItem);
			tag = nmsStack.getTag();
		}
		
		NBTTagList contents = new NBTTagList();
		for (org.bukkit.inventory.ItemStack invStack: stacks) {
			NBTTagCompound itemTag = new NBTTagCompound();
			if (invStack != null && (zipItem == null || !invStack.equals(zipItem))) {
				CraftItemStack.asNMSCopy(invStack).save(itemTag);
			}
						
			contents.add(itemTag);
		}
		tag.set("contents", contents);
		
		return CraftItemStack.asCraftMirror(nmsStack);
	}

	/**
	 * Unzip a "zip" item into an inventory. The "zip" item itself will be add to the inventory too.
	 */
	public static void unzip(org.bukkit.inventory.ItemStack itemStack, Inventory inventory) {
		ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
		if (nmsItemStack == null) {
			return;
		}
		NBTTagCompound tag = nmsItemStack.getTag();
		if (tag == null || !tag.hasKey("contents")) {
			return;
		}
		
		nmsItemStack.setTag(null);
		
		NBTTagList contents = tag.getList("contents", 10);
		
		for (int i = 0; i < contents.size(); i++) {
			NBTTagCompound itemTag = contents.get(i);
			
			ItemStack nmsStack = ItemStack.createStack(itemTag);
			
			if (!itemTag.isEmpty()) {
				inventory.setItem(i, CraftItemStack.asCraftMirror(nmsStack));
			}
		}
		inventory.setItem(Burricos.ZIP_SLOT, itemStack);
	}

	/**
	 * Open a custom view of a horse inventory to the player, to show a simple
	 * inventory view instead of the custom horse UI.
	 */
	public static void openDonkeyContainer(Player player, Horse horse) {
		((CraftPlayer)player).getHandle().openContainer(((CraftHorse)horse).getHandle().inventoryChest);
	}

	/**
	 * Replace horse's inventory reference with a new larger inventory.
	 */
	public static void setLargeDonkeyChest(Horse horse) {
		final EntityHorse handle = ((CraftHorse)horse).getHandle();
		handle.inventoryChest = new InventoryHorseChest("Burrico", 54, handle);
	}

	/**
	 * Reset inventory to a normal donkey inventory.
	 */
	public static void unsetLargeDonkeyChest(Horse horse) {
		horse.getInventory().clear(); // just in case, another player could be looking at the old inventory
		
		EntityHorse handle = ((CraftHorse)horse).getHandle();
		handle.inventoryChest = null; // loadChest needs this, takes care of the rest
		handle.loadChest();
	}
}
