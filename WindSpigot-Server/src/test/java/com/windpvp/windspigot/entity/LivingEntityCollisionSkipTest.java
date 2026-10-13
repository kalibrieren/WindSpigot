package com.windpvp.windspigot.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Regression for EntityLiving living-collision half-rate skip (TODO #15).
 * <p>
 * The old {@code ticksLived % 2} gate was spawn-parity dependent. The replacement
 * keys off the global server tick so all non-players share one schedule.
 */
public class LivingEntityCollisionSkipTest {

	@Test
	public void playersNeverSkipLivingCollisions() {
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(true, true, 0));
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(true, true, 1));
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(true, false, 0));
	}

	@Test
	public void nonPlayersSkipLivingTargetsOnEvenServerTicks() {
		Assert.assertTrue(LivingEntityCollisionSkip.shouldSkip(false, true, 0));
		Assert.assertTrue(LivingEntityCollisionSkip.shouldSkip(false, true, 2));
		Assert.assertTrue(LivingEntityCollisionSkip.shouldSkip(false, true, 100));
	}

	@Test
	public void nonPlayersResolveLivingTargetsOnOddServerTicks() {
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(false, true, 1));
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(false, true, 3));
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(false, true, 101));
	}

	@Test
	public void nonLivingTargetsAreNeverSkippedByThisGate() {
		// Items, arrows, boats, etc. — always resolve regardless of tick parity
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(false, false, 0));
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(false, false, 1));
		Assert.assertFalse(LivingEntityCollisionSkip.shouldSkip(true, false, 0));
	}

	@Test
	public void sharedServerTickScheduleIsDeterministic() {
		// Two non-players on the same server tick must agree (unlike ticksLived parity)
		boolean a = LivingEntityCollisionSkip.shouldSkip(false, true, 42);
		boolean b = LivingEntityCollisionSkip.shouldSkip(false, true, 42);
		Assert.assertEquals(a, b);
		Assert.assertTrue(a);
	}
}
