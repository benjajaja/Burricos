package com.gipsyking.Burricos;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface NMSWrapperInterface {

	/**
	 * "zip" an inventory array into the NBT of a single item. If item is null a new item will be created.
	 */
	public abstract org.bukkit.inventory.ItemStack zip(
			org.bukkit.inventory.ItemStack[] stacks,
			org.bukkit.inventory.ItemStack zipItem);

	/**
	 * Unzip a "zip" item into an inventory. The "zip" item itself will be add to the inventory too.
	 */
	public abstract void unzip(org.bukkit.inventory.ItemStack itemStack,
			Inventory inventory);

	/**
	 * Open a custom view of a horse inventory to the player, to show a simple
	 * inventory view instead of the custom horse UI.
	 */
	public abstract void openDonkeyContainer(Player player, Horse horse);

	/**
	 * Replace horse's inventory reference with a new larger inventory.
	 */
	public abstract void setLargeDonkeyChest(Horse horse);

	/**
	 * Reset inventory to a normal donkey inventory.
	 */
	public abstract void unsetLargeDonkeyChest(Horse horse);

	public abstract boolean isHorseInventory(Inventory inventory);

}