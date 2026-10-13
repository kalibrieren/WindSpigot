package com.windpvp.windspigot.enchant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * EnchantLapisCostTest: vanilla button→lapis mapping and afford/consume rules.
 */
public class EnchantLapisCostTest {

	@Test
	public void vanillaCostMatchesButtonPlusOne() {
		assertEquals(1, EnchantLapisCost.vanillaCost(0));
		assertEquals(2, EnchantLapisCost.vanillaCost(1));
		assertEquals(3, EnchantLapisCost.vanillaCost(2));
	}

	@Test
	public void creativeAlwaysAffords() {
		assertTrue(EnchantLapisCost.canAfford(true, 0, 3));
		assertTrue(EnchantLapisCost.canAfford(true, 0, 99));
	}

	@Test
	public void zeroCostIsFree() {
		assertTrue(EnchantLapisCost.canAfford(false, 0, 0));
		assertTrue(EnchantLapisCost.canAfford(false, 1, 0));
	}

	@Test
	public void survivalRequiresEnoughLapis() {
		assertTrue(EnchantLapisCost.canAfford(false, 3, 3));
		assertTrue(EnchantLapisCost.canAfford(false, 5, 2));
		assertFalse(EnchantLapisCost.canAfford(false, 2, 3));
		assertFalse(EnchantLapisCost.canAfford(false, 0, 1));
	}

	@Test
	public void remainingAfterConsume() {
		assertEquals(2, EnchantLapisCost.remainingAfterConsume(5, 3));
		assertEquals(0, EnchantLapisCost.remainingAfterConsume(2, 2));
		assertEquals(-1, EnchantLapisCost.remainingAfterConsume(1, 2));
		assertEquals(4, EnchantLapisCost.remainingAfterConsume(4, 0));
	}
}
