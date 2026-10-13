package com.windpvp.windspigot.inventory;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.inventory.InventoryWrapper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import net.minecraft.server.IInventory;
import net.minecraft.server.PlayerInventory;
import net.minecraft.server.TileEntityBeacon;

/**
 * Safe casts for CraftBukkit inventory bridges. Custom inventories / atypical
 * viewers used to ClassCastException on unchecked casts left as TODOs.
 */
public final class InventoryCasts {

	private InventoryCasts() {
	}

	/**
	 * Viewer inventory for NMS containers must be a {@link PlayerInventory}.
	 */
	public static PlayerInventory requirePlayerInventory(IInventory inventory, String context) {
		if (inventory instanceof PlayerInventory) {
			return (PlayerInventory) inventory;
		}
		throw new IllegalArgumentException(context + " expected PlayerInventory, got "
				+ (inventory == null ? "null" : inventory.getClass().getName()));
	}

	/**
	 * Prefer an explicit {@link PlayerInventory}; otherwise use the opener's
	 * inventory (NPC / atypical callers that still pass an EntityHuman).
	 */
	public static PlayerInventory resolvePlayerInventory(IInventory inventory, PlayerInventory openerInventory,
			String context) {
		if (inventory instanceof PlayerInventory) {
			return (PlayerInventory) inventory;
		}
		if (openerInventory != null) {
			return openerInventory;
		}
		return requirePlayerInventory(inventory, context);
	}

	/**
	 * Beacon tile → {@link CraftInventoryBeacon}; any other {@link IInventory} →
	 * plain {@link CraftInventory} (custom beacon-like inventories).
	 */
	public static CraftInventory craftInventoryForBeacon(IInventory beacon) {
		if (beacon instanceof TileEntityBeacon) {
			return new CraftInventoryBeacon((TileEntityBeacon) beacon);
		}
		return new CraftInventory(beacon);
	}

	/**
	 * Unwrap Craft inventories; wrap arbitrary Bukkit inventories for NMS slots
	 * (same approach as {@code CraftHumanEntity.openInventory}).
	 */
	public static IInventory asNmsInventory(Inventory inventory) {
		if (inventory == null) {
			throw new IllegalArgumentException("Inventory cannot be null");
		}
		if (inventory instanceof CraftInventory) {
			return ((CraftInventory) inventory).getInventory();
		}
		return new InventoryWrapper(inventory);
	}

	public static CraftHumanEntity requireCraftHumanEntity(HumanEntity player) {
		if (player instanceof CraftHumanEntity) {
			return (CraftHumanEntity) player;
		}
		throw new IllegalArgumentException("HumanEntity must be a CraftHumanEntity, got "
				+ (player == null ? "null" : player.getClass().getName()));
	}

	public static CraftInventory requireCraftInventory(Inventory inventory) {
		if (inventory instanceof CraftInventory) {
			return (CraftInventory) inventory;
		}
		throw new IllegalArgumentException("Inventory must be a CraftInventory, got "
				+ (inventory == null ? "null" : inventory.getClass().getName()));
	}
}
