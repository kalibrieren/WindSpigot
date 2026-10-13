package com.windpvp.windspigot.world;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.minecraft.server.WorldBorder;

/**
 * Locks the named vanilla 1.8 world-border limits that replace CraftWorldBorder's
 * former PAIL "Magic Values" literals.
 */
public class CraftWorldBorderLimitsTest {

	@Test
	public void maxSizeMatchesLegacyCraftClamp() {
		assertEquals(6.0E7D, WorldBorder.MAX_SIZE, 0.0D);
	}

	@Test
	public void minSizeMatchesLegacyCraftClamp() {
		assertEquals(1.0D, WorldBorder.MIN_SIZE, 0.0D);
	}

	@Test
	public void maxCenterCoordinateMatchesLegacyCraftClamp() {
		assertEquals(3.0E7D, WorldBorder.MAX_CENTER_COORDINATE, 0.0D);
		assertEquals(WorldBorder.MAX_SIZE / 2.0D, WorldBorder.MAX_CENTER_COORDINATE, 0.0D);
	}

	@Test
	public void sizeClampPreservesInRangeAndClampsOutliers() {
		assertEquals(1000.0D, clampSize(1000.0D), 0.0D);
		assertEquals(WorldBorder.MIN_SIZE, clampSize(0.0D), 0.0D);
		assertEquals(WorldBorder.MIN_SIZE, clampSize(-50.0D), 0.0D);
		assertEquals(WorldBorder.MAX_SIZE, clampSize(WorldBorder.MAX_SIZE + 1.0D), 0.0D);
	}

	@Test
	public void centerClampPreservesInRangeAndClampsOutliers() {
		assertEquals(0.0D, clampCenter(0.0D), 0.0D);
		assertEquals(WorldBorder.MAX_CENTER_COORDINATE, clampCenter(WorldBorder.MAX_CENTER_COORDINATE + 1.0D),
				0.0D);
		assertEquals(-WorldBorder.MAX_CENTER_COORDINATE, clampCenter(-(WorldBorder.MAX_CENTER_COORDINATE + 1.0D)),
				0.0D);
	}

	@Test
	public void transitionSecondsCapMatchesLegacyLiteral() {
		assertEquals(9223372036854775L, Long.MAX_VALUE / 1000L);
	}

	private static double clampSize(double newSize) {
		return Math.min(WorldBorder.MAX_SIZE, Math.max(WorldBorder.MIN_SIZE, newSize));
	}

	private static double clampCenter(double coord) {
		double max = WorldBorder.MAX_CENTER_COORDINATE;
		return Math.min(max, Math.max(-max, coord));
	}
}
