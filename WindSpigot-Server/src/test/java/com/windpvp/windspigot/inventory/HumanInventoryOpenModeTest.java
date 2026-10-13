package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.windpvp.windspigot.inventory.HumanInventoryOpenMode.Mode;

/**
 * CraftHumanEntity openInventory(InventoryView) NPC vs player routing (TODO #18).
 */
public class HumanInventoryOpenModeTest {

	@Test
	public void realPlayerWithConnectionUsesClientPath() {
		assertEquals(Mode.CLIENT, HumanInventoryOpenMode.resolve(true, true));
	}

	@Test
	public void entityPlayerNpcWithoutConnectionUsesServerSide() {
		// Citizens-style: EntityPlayer, playerConnection == null
		assertEquals(Mode.SERVER_SIDE, HumanInventoryOpenMode.resolve(true, false));
	}

	@Test
	public void nonPlayerHumanEntityUsesServerSide() {
		assertEquals(Mode.SERVER_SIDE, HumanInventoryOpenMode.resolve(false, false));
		// connection flag ignored when not EntityPlayer
		assertEquals(Mode.SERVER_SIDE, HumanInventoryOpenMode.resolve(false, true));
	}
}
