package com.windpvp.windspigot.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;

/**
 * CraftBukkit / Paper rules for lava {@link EntityCombustByBlockEvent}:
 * fire only on first ignition ({@code fireTicks <= 0}) for living entities,
 * and pass the lava combuster block when known (never invent a null when a
 * contact position exists).
 */
public final class LavaCombust {

	private LavaCombust() {
	}

	/**
	 * Whether {@code burnFromLava} should call {@link EntityCombustByBlockEvent}.
	 * Already-burning entities refresh fire without another event (avoids spam).
	 */
	public static boolean shouldCallCombustEvent(boolean living, int fireTicks) {
		return living && fireTicks <= 0;
	}

	/**
	 * Build the lava combust event with a real combuster when available.
	 *
	 * @param combuster lava block (may be null only if contact is unknown)
	 * @param combustee bukkit entity
	 * @param duration  seconds on fire (vanilla lava uses 15)
	 */
	public static EntityCombustEvent createEvent(Block combuster, Entity combustee, int duration) {
		return new EntityCombustByBlockEvent(combuster, combustee, duration);
	}
}
