package com.gipsyking.Burricos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Burricos extends JavaPlugin implements Listener{
	
	static Logger logger;
	
	public static final int ZIP_SLOT = 0; // slots 0 and 1 are saddle and armor respectively
	public static final String ZIP_NAME = "Saddle";
	public static final String ZIP_LORE = "with donkey double chest";
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
	 * donkey inventories will be restored to how they were when the world was
	 * last saved, just like everything else.
	 */
	public void onDisable() {
		int count = 0;
		for (World world: this.getServer().getWorlds()) {
			for (Horse horse: world.getEntitiesByClass(Horse.class)) {
				if (horse.isCarryingChest() && horse.getInventory().getSize() == 54) {
					zip(horse, true);
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
				// This will happen if a donkey was not "unzipped" properly. If humbug
				// doesn't kick player off on logout, if there is a bug in this plugin,
				// if the server crashes, or a donkey went through an end portal.
				// Unzip now and schedule opening of the container so that
				// bukkit/the server reflects the inventory to the
				// player correctly.
				logger.warning(event.getPlayer().getName() + " tried to open a zipped donkey, unzipped donkey and cancelled event");
				final Player player = (Player) event.getPlayer();
				final Horse finalHorse = horse;
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						NMSWrapper.openDonkeyContainer(player, finalHorse);
					}
				});
				event.setCancelled(true);
			}
			return;
		}
		
		event.setCancelled(true);
		// the following InventoryOpenEvent will be prevented by Humbug while riding a donkey:
		NMSWrapper.openDonkeyContainer((Player) event.getPlayer(), horse);
	}
	
	/**
	 * save inventory to "zip" saddle item on any click interaction
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void inventoryClickEvent(InventoryClickEvent event) {
		if (event.getInventory().getSize() != 54) {
			return;
		}
		InventoryHolder holder = event.getInventory().getHolder();
		
		if (holder == null || !(holder instanceof Horse) || !((Horse)holder).isCarryingChest()) {
			return;
		}
		
		// now it is an extended donkey inventory
		
		ItemStack item = event.getCurrentItem();
		if (isZipItem(item)) {
			event.setCancelled(true);
			return;
		}
		
		zip((Horse) holder, false);
	}
	
	/**
	 * Set donkey to extended donkey if it is clicked with chest with upgrade-lore
	 * and it is tame and not already chested.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void setDonkeyDoubleChestEvent(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Horse)) {
			return;
		}
		
		ItemStack chestItem = event.getPlayer().getItemInHand();
		if (chestItem.getType() != Material.CHEST
				|| chestItem.getItemMeta().getLore() == null
				|| chestItem.getItemMeta().getLore().size() != 1
				|| !chestItem.getItemMeta().getLore().get(0).equals(UPGRADE_ITEM_LORE)) {
			// must be used with a chest with exactly that lore, from a Factory or drop
			return;
		}
		
		Horse horse = (Horse) event.getRightClicked();
		Variant variant = horse.getVariant();
		if ((variant != Horse.Variant.DONKEY && variant != Horse.Variant.MULE) || !horse.isTamed() || horse.isCarryingChest()
				|| horse.getInventory().getItem(ZIP_SLOT) != null) {
			return;
		}
		
		event.setCancelled(true);
		horse.setCarryingChest(true);
		chestItem.setAmount(chestItem.getAmount() - 1);
		
		HorseInventory inventory = horse.getInventory();
		NMSWrapper.setLargeDonkeyChest(horse);
		zip(horse, false);
		inventory.clear(); // just in case, another player could be looking at the old inventory
	}
	
	/**
	 * Natural drops include the saddle "zip" item, one chest, and inventory contents.
	 * Remove "zip" saddle item, add upgrade lore on one chest only (there might be
	 * more chests in the inventory).
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void adjustDonkeyDrop(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Horse)) {
			return;
		}
		
		Horse horse = (Horse) entity;
		Variant variant = horse.getVariant();
		if (variant != Horse.Variant.DONKEY && variant != Horse.Variant.MULE) {
			// horse.isCarryingChest is false at this point, maybe because it's dead
			return;
		}
		
		// must copy viewers to avoid ConcurrentModificationException, can't use Iterator either
		ArrayList<HumanEntity> viewers = new ArrayList<HumanEntity>(horse.getInventory().getViewers());
		for (HumanEntity player: viewers) {
			// minecraft closes invs of player viewing a donkey that died too
			player.closeInventory();
		}
		
		List<ItemStack> drops = event.getDrops();
		ItemStack item;
		int i = drops.size() - 1;
		boolean mayDropUpgradeItem = horse.getInventory().getSize() == 54;
		boolean hasRemovedZipItem = false;
		while (i >= 0) {
			item = drops.get(i);
			if (isZipItem(item)) {
				drops.remove(i);
				if (!mayDropUpgradeItem) {
					break;
				}
				hasRemovedZipItem = true;
			} else if (mayDropUpgradeItem && item.getType() == Material.CHEST && item.getItemMeta().getLore() == null) {
				ItemMeta meta = item.getItemMeta();
				meta.setLore(Arrays.asList(new String[]{UPGRADE_ITEM_LORE}));
				item.setItemMeta(meta);
				if (hasRemovedZipItem) {
					break;
				}
				mayDropUpgradeItem = false;
			}
			--i;
		}
	}
	
	/**
	 * When a chunk is unloaded it is saved to disk and removed from memory,
	 * including all entities it contains.
	 * In the case of an extended donkey, all items beyond slot 17 would get
	 * lost. So we "zip" the contents of the extended inventory into the NBT
	 * of the saddle item, which is preserved.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnLoaded(ChunkUnloadEvent event) {
		for (Entity entity: event.getChunk().getEntities()) {
			if (entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if (horse.isCarryingChest() && horse.getInventory().getSize() == 54) {
					zip(horse, true);
				}
			}
		}
	}

	/**
	 * When a chunk is loaded, all entities it contains are also loaded into
	 * memory. This is where we recover extended donkeys inventories from the
	 * "zip" saddle item's NBT, if present.
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
	 * Serialize the inventory of the donkey into the saddle item.
	 * If `unsetLargeDonkeyChest` is true, the inventory is reset to a regular
	 * chested donkey inventory.
	 */
	private void zip(Horse horse, boolean unsetLargeDonkeyChest) {
		ItemStack contents = NMSWrapper.zip(horse.getInventory().getContents(), horse.getInventory().getItem(ZIP_SLOT));
		if (unsetLargeDonkeyChest) {
			NMSWrapper.unsetLargeDonkeyChest(horse);
		}
		horse.getInventory().setItem(ZIP_SLOT, contents);
	}

	/**
	 * If a donkey contains an item that matches the "zip" item lore at the
	 * predefined slot, it is a "zipped" donkey: set inventory to extended and
	 * "unzip" the item to it.
	 */
	private boolean unzip(Horse horse) {
		ItemStack zip = horse.getInventory().getItem(ZIP_SLOT);
		if (zip != null && isZipItem(zip)) {
			NMSWrapper.setLargeDonkeyChest(horse);
			NMSWrapper.unzip(zip, horse.getInventory());
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if item is a "zip" item
	 */
	private boolean isZipItem(ItemStack item) {
		return item.getItemMeta() != null
			&& item.getItemMeta().getLore() != null
			&& item.getItemMeta().getLore().size() == 1
			&& item.getItemMeta().getLore().get(0).equals(ZIP_LORE);
	}
}
