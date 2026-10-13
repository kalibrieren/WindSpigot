package com.windpvp.windspigot.entity;

/**
 * Policy for End-portal exit worlds after CraftBukkit split portal travel into
 * {@code PlayerList.calculateTarget} + {@code Entity.teleportTo(Location)}.
 * <p>
 * The old {@code Entity.teleportTo} body remapped {@code j == 1 && i == 1} to
 * Overworld and forced spawn coordinates. That is obsolete: when the enter
 * dimension is The End, {@code calculateTarget} already switches the exit world
 * to Overworld spawn, and {@code teleportTo} trusts {@code exit.getWorld()}.
 */
public final class PortalEndReturn {

	public static final int NETHER = -1;
	public static final int OVERWORLD = 0;
	public static final int THE_END = 1;

	private PortalEndReturn() {
	}

	/**
	 * Mirrors {@code PlayerList.calculateTarget}: for a non-Nether / non-Overworld
	 * target world (End portal path), an entity already in The End exits to
	 * Overworld spawn rather than staying in The End.
	 *
	 * @param enterDimension        entity's current dimension
	 * @param targetWorldDimension  dimension of the portal target world
	 * @return whether calculateTarget should use Overworld spawn
	 */
	public static boolean usesOverworldSpawn(int enterDimension, int targetWorldDimension) {
		return enterDimension == THE_END && targetWorldDimension != NETHER && targetWorldDimension != OVERWORLD;
	}

	/**
	 * Whether {@code Entity.teleportTo} must still force Overworld when both enter
	 * and exit report dimension 1. Always {@code false}: the exit {@code Location}
	 * already carries the remapped world from {@code calculateTarget} / events.
	 */
	public static boolean teleportToNeedsEndToOverworldOverride() {
		return false;
	}
}
