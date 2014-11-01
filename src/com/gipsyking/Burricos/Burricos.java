package com.gipsyking.Burricos;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.plugin.java.JavaPlugin;

public class Burricos extends JavaPlugin implements Listener{
	
	static Logger logger;
	
	private static final int ZIP_SLOT = 2; // slots 0 and 1 are saddle and armor, 2 is the first normal slot
	public static final String ZIP_LORE = "Donkey.zip"; // a player should never get to see this, but just in case
	public static final String UPGRADE_ITEM_LORE = "Donkey double chest";

	/**
	 * When plugin is loaded, some donkeys that are supposed to be extended
	 * may already have been loaded into memory from disk (entities in the
	 * server spawn area to be precise). Recover those now.
	 */
	public void onEnable(){
		Burricos.logger = getLogger();
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// there may be chunks and entities loaded at this time, and on top of that the may *never* fire ChunkLoadEvent (spawn area)
		int count = 0;
		for (World world: this.getServer().getWorlds()) {
			for (Horse horse: world.getEntitiesByClass(Horse.class)) {
				if (horse.isCarryingChest()) {
					if (unzip(horse)) {
						count++;
					}
				}
			}
		}
		if (count > 0) {
			logger.info("Unzipped " + count + " donkey inventories");
		}
	}
	
	/**
	 * When server is closed by sending Ctr+C to the console (and possibly
	 * SIGKILL to process) a fast shutdown is performed and the world is saved
	 * but no ChunkLoadEvents are fired. Attempt to "zip" extended inventories.
	 * If a player is riding a donkey, it would normally get saved "to the
	 * player" instead of the chunk, but Humbug kicks off players from all
	 * vehicles, so this isn't an issue.
	 * Note: this is not the same as a server crash, in that event all extended
	 * donkey inventories will lose items beyond slot 17, and possibly the
	 * first and second slot.
	 */
	public void onDisable() {
		int count = 0;
		// if the server crashes hard then donkey inv is partially lost (starting from slot 17), no way around that
		for (World world: this.getServer().getWorlds()) {
			for (Horse horse: world.getEntitiesByClass(Horse.class)) {
				if (zip(horse)) {
					count++;
				}
			}
		}
		if (count > 0) {
			logger.info("Zipped " + count + " donkey inventories");
		}
	}

	/**
	 * If a horse inventory is opened and that horse is chested and it has 54
	 * slots, then we cancel and open a custom 54 slot view but with the horse
	 * as handle, so that everything else is handled by the server in vanilla
	 * style. The opening of the custom view will fire another event that is
	 * not cancelled (by this plugin).
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void inventoryOpenEvent(InventoryOpenEvent event) {
		if (!(event.getInventory() instanceof HorseInventory)) {
			return;
		}
		Horse horse = (Horse) event.getInventory().getHolder();
		if (!((Horse)event.getInventory().getHolder()).isCarryingChest()) {
			return;
		}
		
		if (horse.getInventory().getSize() != 54) {
			// it's not an upgraded donkey, return but check if needs unzipping
			if (unzip(horse)) {
				// this could happen if Humbug does not kick a player off a donkey for
				// some reason (anything but PlayerQuitEvent: server stop for example)
				// or if there is a bug.
				logger.warning(event.getPlayer().getName() + " tried to open a zipped donkey, unzipped donkey and cancelled event");
				event.setCancelled(true);
			}
			return;
		}
		
		event.setCancelled(true);
		// sadly, this will do nothing while riding a donkey:
		NMSWrapper.openDonkeyContainer((Player) event.getPlayer(), horse);
	}
	
	/**
	 * Set donkey to extended donkey if it is clicked with the special chest
	 * and it is tame and not already chested.
	 * Attempt to copy existing inventory (could only contain a saddle).
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void setDonkeyDoubleChestEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Horse)) {
			return;
		}
		Horse horse = (Horse) event.getRightClicked();
		if (!horse.isTamed() || horse.isCarryingChest()) {
			return;
		}
		
		ItemStack chestItem = event.getPlayer().getItemInHand();
		if (chestItem.getType() != Material.CHEST
				|| chestItem.getItemMeta().getLore() == null
				|| chestItem.getItemMeta().getLore().size() != 1
				|| !chestItem.getItemMeta().getLore().get(0).equals(UPGRADE_ITEM_LORE)) {
			// must be used with a chest with exactly that lore, from a Factory or something
			return;
		}
		
		
		event.setCancelled(true);
		horse.setCarryingChest(true);
		chestItem.setAmount(chestItem.getAmount() - 1);
		
		HorseInventory inventory = horse.getInventory();
		NMSWrapper.setLargeDonkeyChest(horse);
		// copy items over, if donkey already had some. Can only contain saddle actually.
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null) {
				horse.getInventory().setItem(i, inventory.getItem(i).clone());
			}
		}
		inventory.clear(); // just in case, another player could be looking at the old inventory
	}
	
	/**
	 * When a chunk is unloaded it is saved to disk and removed from memory,
	 * including all entities it contains.
	 * In the case of an extended donkey, all items beyond slot 17 would get
	 * lost. So we "zip" the contents of the extended inventory into the NBT
	 * of a single item, which is preserved.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest() && horse.getInventory().getSize() == 54) {
					zip(horse);
				}
			}
		}
	}

	/**
	 * When a chunk is loaded, all entities it contains are also loaded into
	 * memory. This is where we recover extended donkeys inventories from the
	 * "zip" item's NBT, if present.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoaded(ChunkLoadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest()) {
					unzip(horse);
				}
			}
		}
	}

	/**
	 * Serialize the inventory of the donkey into an item, clear donkey
	 * inventory, and add only the item. This will be saved to disk
	 * successfully.
	 */
	private boolean zip(Horse horse) {
		if (horse.isCarryingChest() && horse.getInventory().getSize() == 54) {
			ItemStack contents = NMSWrapper.zip(horse.getInventory().getContents());
			NMSWrapper.unsetLargeDonkeyChest(horse);
			horse.getInventory().setItem(ZIP_SLOT, contents);
			return true;
		}
		return false;
	}

	/*
	 * If a donkey contains an item that matches the "zip" item lore at the
	 * predefined slot, it is a "zipped" donkey: set inventory to extended and
	 * "unzip" the item to it.
	 */
	private boolean unzip(Horse horse) {
		ItemStack zip = horse.getInventory().getItem(ZIP_SLOT);
		if (zip != null
				&& zip.getItemMeta() != null
				&& zip.getItemMeta().getLore() != null
				&& zip.getItemMeta().getLore().size() == 1
				&& zip.getItemMeta().getLore().get(0).equals(ZIP_LORE)) {
			NMSWrapper.setLargeDonkeyChest(horse);
			NMSWrapper.unzip(zip, horse.getInventory());
			return true;
		}
		return false;
	}
}
