package com.windpvp.windspigot.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Regression for Entity NBT save rotation TODO (NaN/Inf yaw/pitch).
 */
public class EntityRotationTest {

	@Test
	public void passesFiniteValuesUnchanged() {
		Assert.assertEquals(0.0F, EntityRotation.sanitize(0.0F), 0.0F);
		Assert.assertEquals(90.0F, EntityRotation.sanitize(90.0F), 0.0F);
		Assert.assertEquals(-180.0F, EntityRotation.sanitize(-180.0F), 0.0F);
		Assert.assertEquals(359.9F, EntityRotation.sanitize(359.9F), 0.0F);
	}

	@Test
	public void replacesNaNWithZero() {
		Assert.assertEquals(0.0F, EntityRotation.sanitize(Float.NaN), 0.0F);
	}

	@Test
	public void replacesInfinityWithZero() {
		Assert.assertEquals(0.0F, EntityRotation.sanitize(Float.POSITIVE_INFINITY), 0.0F);
		Assert.assertEquals(0.0F, EntityRotation.sanitize(Float.NEGATIVE_INFINITY), 0.0F);
	}
}
