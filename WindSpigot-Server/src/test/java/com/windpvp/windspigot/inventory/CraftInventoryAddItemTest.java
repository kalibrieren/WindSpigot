package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.server.Blocks;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.Item;

/**
 * Behavioral coverage for optimized {@code CraftInventory#addItem}: merge into
 * partials, spill across stacks, leftover when full, and mixed Materials in one
 * batch (per-Material lastPartial cursor + cached firstEmpty).
 * <p>
 * Uses NMS-backed {@link CraftItemStack}s so tests do not require a live
 * {@code Bukkit} server / ItemFactory.
 */
public class CraftInventoryAddItemTest {

	@BeforeClass
	public static void bootstrapNms() {
		DispenserRegistry.c();
	}

	private static ItemStack stack(net.minecraft.server.Block block, int amount) {
		return CraftItemStack.asNewCraftStack(Item.getItemOf(block), amount);
	}

	private static ItemStack wool(int amount, short color) {
		ItemStack stack = stack(Blocks.WOOL, amount);
		stack.setDurability(color);
		return stack;
	}

	@Test
	public void mergesIntoExistingPartialThenUsesEmptySlots() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 4);
		inv.setItem(0, stack(Blocks.COBBLESTONE, 60));
		inv.setItem(2, stack(Blocks.DIRT, 10));

		HashMap<Integer, ItemStack> leftover = inv.addItem(stack(Blocks.COBBLESTONE, 20));

		assertTrue(leftover.isEmpty());
		assertEquals(64, inv.getItem(0).getAmount());
		assertEquals(stack(Blocks.COBBLESTONE, 1).getType(), inv.getItem(1).getType());
		assertEquals(16, inv.getItem(1).getAmount());
		assertEquals(stack(Blocks.DIRT, 1).getType(), inv.getItem(2).getType());
		assertEquals(10, inv.getItem(2).getAmount());
	}

	@Test
	public void batchOfSameMaterialFillsPartialsAndEmpties() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 3);
		inv.setItem(0, stack(Blocks.STONE, 50));

		HashMap<Integer, ItemStack> leftover = inv.addItem(stack(Blocks.STONE, 20), stack(Blocks.STONE, 64),
				stack(Blocks.STONE, 10));

		assertTrue(leftover.isEmpty());
		assertEquals(64, inv.getItem(0).getAmount());
		assertEquals(64, inv.getItem(1).getAmount());
		assertEquals(16, inv.getItem(2).getAmount());
	}

	@Test
	public void returnsLeftoverWhenInventoryIsFull() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 1);
		inv.setItem(0, stack(Blocks.STONE, 64));

		ItemStack overflow = stack(Blocks.STONE, 5);
		HashMap<Integer, ItemStack> leftover = inv.addItem(overflow);

		assertEquals(1, leftover.size());
		assertEquals(5, leftover.get(Integer.valueOf(0)).getAmount());
		assertEquals(64, inv.getItem(0).getAmount());
	}

	@Test
	public void mixedMaterialsDoNotSkipEarlierPartials() {
		// Same Material (WOOL) with different durability must not share a blind cursor.
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 4);
		inv.setItem(0, wool(32, (short) 0));
		inv.setItem(1, wool(32, (short) 1));

		HashMap<Integer, ItemStack> leftover = inv.addItem(wool(16, (short) 1), wool(16, (short) 0));

		assertTrue(leftover.isEmpty());
		assertEquals(48, inv.getItem(0).getAmount());
		assertEquals(0, inv.getItem(0).getDurability());
		assertEquals(48, inv.getItem(1).getAmount());
		assertEquals(1, inv.getItem(1).getDurability());
	}

	@Test
	public void oversizedStackSplitsAcrossEmptySlots() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 3);

		HashMap<Integer, ItemStack> leftover = inv.addItem(stack(Blocks.COBBLESTONE, 100));

		assertTrue(leftover.isEmpty());
		assertEquals(64, inv.getItem(0).getAmount());
		assertEquals(36, inv.getItem(1).getAmount());
		assertNull(inv.getItem(2));
	}
}
