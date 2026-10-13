package com.windpvp.windspigot.entity;

import org.bukkit.event.hanging.HangingBreakEvent;

/**
 * Classifies hanging-entity (item frame / painting) break causes for Bukkit events.
 * <p>
 * Obstruction must be decided from solid collision geometry intersecting the hanging
 * bounding box — not from a single {@code BlockPosition} material sample at the entity
 * origin, which misses large paintings and partial block overlaps.
 * <p>
 * Piston pushes go through {@code EntityHanging#move} (invoked by
 * {@code net.minecraft.server.TileEntityPiston}); that path is not a {@code survives()}
 * failure, so it uses {@link #forPistonPush()} instead of {@link #whenUnsurviving(boolean)}.
 */
public final class HangingBreakCauses {

	private HangingBreakCauses() {
	}

	/**
	 * @param obstructedByBlocks {@code true} when {@code world.getCubes(hanging, bb)} is
	 *                           non-empty (same criterion as the first {@code survives()} check)
	 * @return {@link HangingBreakEvent.RemoveCause#OBSTRUCTION} when clipped by a block,
	 *         otherwise {@link HangingBreakEvent.RemoveCause#PHYSICS} (support gone, overlap, etc.)
	 */
	public static HangingBreakEvent.RemoveCause whenUnsurviving(boolean obstructedByBlocks) {
		return obstructedByBlocks ? HangingBreakEvent.RemoveCause.OBSTRUCTION
				: HangingBreakEvent.RemoveCause.PHYSICS;
	}

	/**
	 * Cause for hanging entities broken by a non-zero {@code move()} — in vanilla NMS this is
	 * only invoked for hangings by piston extension. Distinct from
	 * {@link HangingBreakEvent.RemoveCause#PHYSICS} (support block destroyed / overlap).
	 */
	public static HangingBreakEvent.RemoveCause forPistonPush() {
		return HangingBreakEvent.RemoveCause.PISTON;
	}
}
