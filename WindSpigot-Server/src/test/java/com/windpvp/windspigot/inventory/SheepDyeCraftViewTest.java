package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.junit.Test;

import net.minecraft.server.Container;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.InventoryCraftResult;
import net.minecraft.server.InventoryCrafting;

/**
 * Regression for EntitySheep {@code TODO: O.O} — dye-mix Container must expose a
 * real InventoryView (never null) for PrepareItemCraftEvent during breeding.
 */
public class SheepDyeCraftViewTest {

	@Test
	public void createReturnsNonNullViewWithCraftingTopAndNullPlayer() {
		InventoryCrafting matrix = newMatrix();
		InventoryView view = SheepDyeCraftView.create(matrix);

		assertNotNull(view);
		assertNull(view.getPlayer());
		assertTrue(view.getTopInventory() instanceof CraftingInventory);
		assertNotNull(view.getBottomInventory());
		assertEquals(0, view.getBottomInventory().getSize());
		// 2-slot matrix → CraftInventory maps size < 9 to CRAFTING
		assertEquals(InventoryType.CRAFTING, view.getType());
	}

	@Test
	public void createRejectsNullMatrixOrMissingResultSlot() {
		try {
			SheepDyeCraftView.create(null);
			fail("expected IllegalArgumentException for null matrix");
		} catch (IllegalArgumentException expected) {
			assertEquals("matrix", expected.getMessage());
		}

		InventoryCrafting matrix = new InventoryCrafting(dummyContainer(), 2, 1);
		try {
			SheepDyeCraftView.create(matrix);
			fail("expected IllegalArgumentException for missing resultInventory");
		} catch (IllegalArgumentException expected) {
			assertEquals("matrix.resultInventory", expected.getMessage());
		}
	}

	@Test
	public void containerGetBukkitViewIsStableAndNonNull() {
		final InventoryCrafting[] holder = new InventoryCrafting[1];
		Container container = new Container() {
			private InventoryView cached;

			@Override
			public boolean a(EntityHuman entityhuman) {
				return false;
			}

			@Override
			public InventoryView getBukkitView() {
				if (cached == null) {
					cached = SheepDyeCraftView.create(holder[0]);
				}
				return cached;
			}
		};
		holder[0] = new InventoryCrafting(container, 2, 1);
		holder[0].resultInventory = new InventoryCraftResult();

		InventoryView first = container.getBukkitView();
		InventoryView second = container.getBukkitView();
		assertNotNull(first);
		assertSame(first, second);
		assertNull(first.getPlayer());
	}

	private static InventoryCrafting newMatrix() {
		InventoryCrafting matrix = new InventoryCrafting(dummyContainer(), 2, 1);
		matrix.resultInventory = new InventoryCraftResult();
		return matrix;
	}

	private static Container dummyContainer() {
		return new Container() {
			@Override
			public boolean a(EntityHuman entityhuman) {
				return false;
			}

			@Override
			public InventoryView getBukkitView() {
				return null;
			}
		};
	}
}
