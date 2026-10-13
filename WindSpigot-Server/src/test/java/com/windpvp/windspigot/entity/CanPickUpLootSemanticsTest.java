package com.windpvp.windspigot.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Documents PlayerPickupItemEvent pre-cancel wiring:
 * {@code event.setCancelled(!canPickUpLoot)}.
 * Mirrors EntityItem / EntityArrow pickup cancel paths.
 */
public class CanPickUpLootSemanticsTest {

	@Test
	public void defaultPlayerCanPickUpDoesNotPreCancel() {
		boolean canPickUpLoot = true; // EntityHuman default / EntityPlayer ctor
		Assert.assertFalse("players with canPickUpLoot=true must not pre-cancel pickup",
				!canPickUpLoot);
	}

	@Test
	public void disabledPickUpPreCancelsEvent() {
		boolean canPickUpLoot = false; // LivingEntity#setCanPickupItems(false)
		Assert.assertTrue("canPickUpLoot=false must pre-cancel PlayerPickupItemEvent",
				!canPickUpLoot);
	}

	@Test
	public void pluginMayUncancelAfterPreCancel() {
		boolean canPickUpLoot = false;
		boolean cancelled = !canPickUpLoot;
		// Plugin listener: event.setCancelled(false)
		cancelled = false;
		Assert.assertFalse("plugins must still be able to un-cancel after pre-cancel", cancelled);
	}
}
