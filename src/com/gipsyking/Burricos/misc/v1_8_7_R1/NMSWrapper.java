package com.gipsyking.Burricos.misc.v1_8_7_R1;

// all the cancer, at least it's in one place
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.InventoryHorseChest;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.ItemStack;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryHorse;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;

import com.gipsyking.Burricos.Burricos;
import com.gipsyking.Burricos.NMSWrapperInterface;


public class NMSWrapper implements NMSWrapperInterface {

	/* (non-Javadoc)
	 * @see com.gipsyking.Burricos.NMSWrapperInterface#zip(org.bukkit.inventory.ItemStack[], org.bukkit.inventory.ItemStack)
	 */
	@Override
	public org.bukkit.inventory.ItemStack zip(org.bukkit.inventory.ItemStack[] stacks, org.bukkit.inventory.ItemStack zipItem) {
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

	/* (non-Javadoc)
	 * @see com.gipsyking.Burricos.NMSWrapperInterface#unzip(org.bukkit.inventory.ItemStack, org.bukkit.inventory.Inventory)
	 */
	@Override
	public void unzip(org.bukkit.inventory.ItemStack itemStack, Inventory inventory) {
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

	/* (non-Javadoc)
	 * @see com.gipsyking.Burricos.NMSWrapperInterface#openDonkeyContainer(org.bukkit.entity.Player, org.bukkit.entity.Horse)
	 */
	@Override
	public void openDonkeyContainer(Player player, Horse horse) {
		((CraftPlayer)player).getHandle().openContainer(((CraftHorse)horse).getHandle().inventoryChest);
	}

	/* (non-Javadoc)
	 * @see com.gipsyking.Burricos.NMSWrapperInterface#setLargeDonkeyChest(org.bukkit.entity.Horse)
	 */
	@Override
	public void setLargeDonkeyChest(Horse horse) {
		final EntityHorse handle = ((CraftHorse)horse).getHandle();
		handle.inventoryChest = new InventoryHorseChest("Burrico", 54, handle);
	}

	/* (non-Javadoc)
	 * @see com.gipsyking.Burricos.NMSWrapperInterface#unsetLargeDonkeyChest(org.bukkit.entity.Horse)
	 */
	@Override
	public void unsetLargeDonkeyChest(Horse horse) {
		horse.getInventory().clear(); // just in case, another player could be looking at the old inventory
		
		EntityHorse handle = ((CraftHorse)horse).getHandle();
		handle.inventoryChest = null; // loadChest needs this, takes care of the rest
		handle.loadChest();
	}

	/* (non-Javadoc)
	 * @see com.gipsyking.Burricos.NMSWrapperInterface#isHorseInventory(org.bukkit.inventory.Inventory)
	 */
	@Override
	public boolean isHorseInventory(Inventory inventory) {
		return (inventory instanceof HorseInventory) || (inventory instanceof CraftInventoryHorse);
	}
}
