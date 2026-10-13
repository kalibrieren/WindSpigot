package com.windpvp.windspigot.entity;

/**
 * Decides whether a living entity should skip resolving a living-entity push this tick.
 * <p>
 * CraftBukkit historically skipped non-player living collisions on every other
 * {@code ticksLived} tick. That per-entity counter is a poor schedule: spawn-time
 * parity makes mob-mob pairs either double-apply or never apply on a given tick,
 * and it drifts relative to activation-range / partial ticking. WindSpigot keeps
 * the half-rate mob-mob optimization (players never skip) but keys it off the
 * global {@link net.minecraft.server.MinecraftServer#currentTick} so every
 * non-player shares one intentional skip schedule.
 */
public final class LivingEntityCollisionSkip {

	private LivingEntityCollisionSkip() {
	}

	/**
	 * @param selfIsPlayer  whether the entity running the push loop is a player
	 * @param otherIsLiving whether the candidate collidee is a living entity
	 * @param serverTick    {@code MinecraftServer.currentTick}
	 * @return true if this living-living push should be skipped this tick
	 */
	public static boolean shouldSkip(boolean selfIsPlayer, boolean otherIsLiving, int serverTick) {
		// Players always resolve (responsive player↔mob / PvP feel). Non-players only
		// skip living targets, and only on even server ticks (~half rate).
		return !selfIsPlayer && otherIsLiving && (serverTick & 1) == 0;
	}
}
