package com.windpvp.windspigot.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import net.minecraft.server.InventoryCrafting;

/**
 * Synthetic {@link InventoryView} for {@code EntitySheep}'s internal 2-slot dye
 * mix matrix. That container is never opened by a player ({@code canUse} is
 * always false); CraftBukkit still requires {@code Container#getBukkitView()}.
 * <p>
 * Returning {@code null} (legacy {@code TODO: O.O}) left
 * {@code PrepareItemCraftEvent} during breeding with a null or stale player
 * craft view. This view exposes the dye matrix so plugins see a stable
 * inventory; {@link #getPlayer()} is {@code null} because no human owns the
 * window.
 */
public final class SheepDyeCraftView {

	private SheepDyeCraftView() {
	}

	public static InventoryView create(InventoryCrafting matrix) {
		if (matrix == null) {
			throw new IllegalArgumentException("matrix");
		}
		if (matrix.resultInventory == null) {
			throw new IllegalArgumentException("matrix.resultInventory");
		}

		final CraftInventoryCrafting top = new CraftInventoryCrafting(matrix, matrix.resultInventory);
		final Inventory bottom = new CraftInventoryCustom(null, 0);

		return new InventoryView() {
			@Override
			public Inventory getTopInventory() {
				return top;
			}

			@Override
			public Inventory getBottomInventory() {
				return bottom;
			}

			@Override
			public HumanEntity getPlayer() {
				return null;
			}

			@Override
			public InventoryType getType() {
				return top.getType();
			}
		};
	}
}
