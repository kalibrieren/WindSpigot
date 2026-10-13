package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * WindowClickMappingTest: slot bounds and held-slot SetSlot resync rules.
 */
public class WindowClickMappingTest {

	@Test
	public void acceptsInRangeRawSlots() {
		assertTrue(WindowClickMapping.isValidRawSlot(0, 45));
		assertTrue(WindowClickMapping.isValidRawSlot(44, 45));
	}

	@Test
	public void rejectsOutsideAndOversizedRawSlots() {
		assertFalse(WindowClickMapping.isValidRawSlot(-999, 45)); // client "outside"
		assertFalse(WindowClickMapping.isValidRawSlot(-1, 45));
		assertFalse(WindowClickMapping.isValidRawSlot(45, 45));
		assertFalse(WindowClickMapping.isValidRawSlot(100, 45));
		assertFalse(WindowClickMapping.isValidRawSlot(0, 0));
	}

	@Test
	public void resyncWhenHandDiffersFromPacket() {
		assertTrue(WindowClickMapping.shouldResyncHeldSlot(false, false));
	}

	@Test
	public void resyncWhenAlwaysForcedEvenIfStacksMatch() {
		// Cancelled / throttled place: matches true, client still predicted change
		assertTrue(WindowClickMapping.shouldResyncHeldSlot(true, true));
	}

	@Test
	public void skipResyncWhenStacksMatchAndNotForced() {
		assertFalse(WindowClickMapping.shouldResyncHeldSlot(true, false));
	}
}
