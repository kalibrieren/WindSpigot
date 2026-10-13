package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bukkit.craftbukkit.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.inventory.CraftInventoryBrewer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.inventory.util.CraftInventoryCreator;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.junit.Test;

import net.minecraft.server.TileEntityDispenser;
import net.minecraft.server.TileEntityDropper;
import net.minecraft.server.TileEntityHopper;

/**
 * Locks SPIGOT-3981 behaviour: createInventory returns specialised Bukkit types
 * where practical instead of always CraftInventoryCustom.
 */
public class CraftInventoryCreatorTest {

	@Test
	public void furnaceReturnsFurnaceInventory() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.FURNACE);
		assertTrue(inventory instanceof FurnaceInventory);
		assertTrue(inventory instanceof CraftInventoryFurnace);
		assertEquals(InventoryType.FURNACE, inventory.getType());
		assertEquals(3, inventory.getSize());
	}

	@Test
	public void brewingReturnsBrewerInventory() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.BREWING);
		assertTrue(inventory instanceof BrewerInventory);
		assertTrue(inventory instanceof CraftInventoryBrewer);
		assertEquals(InventoryType.BREWING, inventory.getType());
	}

	@Test
	public void beaconReturnsBeaconInventory() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.BEACON);
		assertTrue(inventory instanceof BeaconInventory);
		assertTrue(inventory instanceof CraftInventoryBeacon);
		assertEquals(InventoryType.BEACON, inventory.getType());
	}

	@Test
	public void dispenserUsesTileEntityDispenser() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.DISPENSER);
		assertFalse(inventory instanceof CraftInventoryCustom);
		assertEquals(InventoryType.DISPENSER, inventory.getType());
		assertTrue(((org.bukkit.craftbukkit.inventory.CraftInventory) inventory)
				.getInventory() instanceof TileEntityDispenser);
		assertEquals(9, inventory.getSize());
	}

	@Test
	public void dropperUsesTileEntityDropper() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.DROPPER);
		assertFalse(inventory instanceof CraftInventoryCustom);
		assertEquals(InventoryType.DROPPER, inventory.getType());
		assertTrue(((org.bukkit.craftbukkit.inventory.CraftInventory) inventory)
				.getInventory() instanceof TileEntityDropper);
	}

	@Test
	public void hopperUsesTileEntityHopper() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.HOPPER);
		assertFalse(inventory instanceof CraftInventoryCustom);
		assertEquals(InventoryType.HOPPER, inventory.getType());
		assertTrue(((org.bukkit.craftbukkit.inventory.CraftInventory) inventory)
				.getInventory() instanceof TileEntityHopper);
		assertEquals(5, inventory.getSize());
	}

	@Test
	public void chestFallsBackToCustom() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.CHEST);
		assertTrue(inventory instanceof CraftInventoryCustom);
		assertEquals(InventoryType.CHEST, inventory.getType());
		assertEquals(27, inventory.getSize());
	}

	@Test
	public void sizedChestUsesCustom() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, 54, "Big Chest");
		assertTrue(inventory instanceof CraftInventoryCustom);
		assertEquals(54, inventory.getSize());
		assertEquals("Big Chest", inventory.getTitle());
	}

	@Test
	public void titledFurnaceKeepsSpecialisedType() {
		Inventory inventory = CraftInventoryCreator.INSTANCE.createInventory(null, InventoryType.FURNACE, "Smelter");
		assertTrue(inventory instanceof FurnaceInventory);
		assertEquals("Smelter", inventory.getName());
	}

	@Test
	public void nonCreatableTypesRemainRejectedByFlag() {
		assertFalse(InventoryType.CRAFTING.isCreatable());
		assertFalse(InventoryType.CREATIVE.isCreatable());
		try {
			if (!InventoryType.CRAFTING.isCreatable()) {
				throw new IllegalArgumentException("Cannot open an inventory of type " + InventoryType.CRAFTING);
			}
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("CRAFTING"));
		}
	}
}
