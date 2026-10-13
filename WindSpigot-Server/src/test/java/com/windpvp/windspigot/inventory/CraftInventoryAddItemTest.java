package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.server.DispenserRegistry;

/**
 * Behavioral coverage for optimized {@code CraftInventory#addItem}: merge into
 * partials, spill across stacks, leftover when full, and mixed Materials in one
 * batch (per-Material lastPartial cursor + cached firstEmpty).
 */
public class CraftInventoryAddItemTest {

	@BeforeClass
	public static void bootstrapNms() {
		DispenserRegistry.c();
	}

	@Test
	public void mergesIntoExistingPartialThenUsesEmptySlots() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 4);
		inv.setItem(0, new ItemStack(Material.COBBLESTONE, 60));
		inv.setItem(2, new ItemStack(Material.DIRT, 10));

		HashMap<Integer, ItemStack> leftover = inv.addItem(new ItemStack(Material.COBBLESTONE, 20));

		assertTrue(leftover.isEmpty());
		assertEquals(64, inv.getItem(0).getAmount());
		assertEquals(Material.COBBLESTONE, inv.getItem(1).getType());
		assertEquals(16, inv.getItem(1).getAmount());
		assertEquals(Material.DIRT, inv.getItem(2).getType());
		assertEquals(10, inv.getItem(2).getAmount());
	}

	@Test
	public void batchOfSameMaterialFillsPartialsAndEmpties() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 3);
		inv.setItem(0, new ItemStack(Material.STONE, 50));

		HashMap<Integer, ItemStack> leftover = inv.addItem(new ItemStack(Material.STONE, 20),
				new ItemStack(Material.STONE, 64), new ItemStack(Material.STONE, 10));

		assertTrue(leftover.isEmpty());
		assertEquals(64, inv.getItem(0).getAmount());
		assertEquals(64, inv.getItem(1).getAmount());
		assertEquals(16, inv.getItem(2).getAmount());
	}

	@Test
	public void returnsLeftoverWhenInventoryIsFull() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 1);
		inv.setItem(0, new ItemStack(Material.STONE, 64));

		ItemStack overflow = new ItemStack(Material.STONE, 5);
		HashMap<Integer, ItemStack> leftover = inv.addItem(overflow);

		assertEquals(1, leftover.size());
		assertEquals(5, leftover.get(Integer.valueOf(0)).getAmount());
		assertEquals(64, inv.getItem(0).getAmount());
	}

	@Test
	public void mixedMaterialsDoNotSkipEarlierPartials() {
		// Same Material (WOOL) with different durability must not share a blind cursor.
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 4);
		ItemStack whitePartial = new ItemStack(Material.WOOL, 32);
		whitePartial.setDurability((short) 0);
		ItemStack orangePartial = new ItemStack(Material.WOOL, 32);
		orangePartial.setDurability((short) 1);
		inv.setItem(0, whitePartial);
		inv.setItem(1, orangePartial);

		ItemStack addOrange = new ItemStack(Material.WOOL, 16);
		addOrange.setDurability((short) 1);
		ItemStack addWhite = new ItemStack(Material.WOOL, 16);
		addWhite.setDurability((short) 0);

		HashMap<Integer, ItemStack> leftover = inv.addItem(addOrange, addWhite);

		assertTrue(leftover.isEmpty());
		assertEquals(48, inv.getItem(0).getAmount());
		assertEquals(0, inv.getItem(0).getDurability());
		assertEquals(48, inv.getItem(1).getAmount());
		assertEquals(1, inv.getItem(1).getDurability());
	}

	@Test
	public void oversizedStackSplitsAcrossEmptySlots() {
		CraftInventoryCustom inv = new CraftInventoryCustom(null, 3);

		HashMap<Integer, ItemStack> leftover = inv.addItem(new ItemStack(Material.COBBLESTONE, 100));

		assertTrue(leftover.isEmpty());
		assertEquals(64, inv.getItem(0).getAmount());
		assertEquals(36, inv.getItem(1).getAmount());
		assertEquals(null, inv.getItem(2));
	}
}
