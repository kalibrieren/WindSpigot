package com.windpvp.windspigot.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Regression for Entity.teleportTo commented End→End remap (TODO #3 / rank 26).
 * <p>
 * BlockEnderPortal always calls {@code entity.c(1)}. Returning from The End used
 * to be handled inside teleportTo ({@code j == 1 && i == 1}); CraftBukkit moved
 * that into {@code PlayerList.calculateTarget}.
 */
public class PortalEndReturnTest {

	@Test
	public void endPortalWhileInEndUsesOverworldSpawn() {
		Assert.assertTrue(PortalEndReturn.usesOverworldSpawn(PortalEndReturn.THE_END, PortalEndReturn.THE_END));
	}

	@Test
	public void overworldToEndKeepsEndSpawn() {
		Assert.assertFalse(PortalEndReturn.usesOverworldSpawn(PortalEndReturn.OVERWORLD, PortalEndReturn.THE_END));
	}

	@Test
	public void netherOverworldScalePathsDoNotUseEndReturn() {
		Assert.assertFalse(PortalEndReturn.usesOverworldSpawn(PortalEndReturn.OVERWORLD, PortalEndReturn.NETHER));
		Assert.assertFalse(PortalEndReturn.usesOverworldSpawn(PortalEndReturn.NETHER, PortalEndReturn.OVERWORLD));
		Assert.assertFalse(PortalEndReturn.usesOverworldSpawn(PortalEndReturn.THE_END, PortalEndReturn.NETHER));
		Assert.assertFalse(PortalEndReturn.usesOverworldSpawn(PortalEndReturn.THE_END, PortalEndReturn.OVERWORLD));
	}

	@Test
	public void teleportToMustNotReapplyEndToOverworldOverride() {
		Assert.assertFalse(PortalEndReturn.teleportToNeedsEndToOverworldOverride());
	}
}
