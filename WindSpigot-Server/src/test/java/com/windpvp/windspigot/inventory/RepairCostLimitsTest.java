package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * RepairCostLimitsTest: no API clamp on repair penalty; presence is {@code > 0}.
 */
public class RepairCostLimitsTest {

	@Test
	public void presentOnlyWhenPositive() {
		assertFalse(RepairCostLimits.isPresent(0));
		assertFalse(RepairCostLimits.isPresent(-1));
		assertFalse(RepairCostLimits.isPresent(Integer.MIN_VALUE));
		assertTrue(RepairCostLimits.isPresent(1));
		assertTrue(RepairCostLimits.isPresent(39));
		assertTrue(RepairCostLimits.isPresent(40));
		assertTrue(RepairCostLimits.isPresent(Integer.MAX_VALUE));
	}

	@Test
	public void applyIsIdentityAcrossIntRange() {
		assertEquals(0, RepairCostLimits.apply(0));
		assertEquals(-5, RepairCostLimits.apply(-5));
		assertEquals(1, RepairCostLimits.apply(1));
		assertEquals(40, RepairCostLimits.apply(40));
		assertEquals(Integer.MAX_VALUE, RepairCostLimits.apply(Integer.MAX_VALUE));
		assertEquals(Integer.MIN_VALUE, RepairCostLimits.apply(Integer.MIN_VALUE));
	}
}
