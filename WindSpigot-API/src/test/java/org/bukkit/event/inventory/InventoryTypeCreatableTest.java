package org.bukkit.event.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InventoryTypeCreatableTest {

	@Test
	public void craftingAndCreativeAreNotCreatable() {
		assertFalse(InventoryType.CRAFTING.isCreatable());
		assertFalse(InventoryType.CREATIVE.isCreatable());
	}

	@Test
	public void specialisedAndCustomTypesAreCreatable() {
		assertTrue(InventoryType.CHEST.isCreatable());
		assertTrue(InventoryType.DISPENSER.isCreatable());
		assertTrue(InventoryType.DROPPER.isCreatable());
		assertTrue(InventoryType.FURNACE.isCreatable());
		assertTrue(InventoryType.WORKBENCH.isCreatable());
		assertTrue(InventoryType.ENCHANTING.isCreatable());
		assertTrue(InventoryType.BREWING.isCreatable());
		assertTrue(InventoryType.PLAYER.isCreatable());
		assertTrue(InventoryType.MERCHANT.isCreatable());
		assertTrue(InventoryType.ENDER_CHEST.isCreatable());
		assertTrue(InventoryType.ANVIL.isCreatable());
		assertTrue(InventoryType.BEACON.isCreatable());
		assertTrue(InventoryType.HOPPER.isCreatable());
	}
}
