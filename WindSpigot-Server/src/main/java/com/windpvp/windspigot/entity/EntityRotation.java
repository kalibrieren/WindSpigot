package com.windpvp.windspigot.entity;

/**
 * Sanitizes entity yaw/pitch for CraftBukkit paths that can see NaN or
 * non-finite values (direct field writes, corrupted NBT, plugin teleports).
 * <p>
 * {@code Entity#setYawPitch} already rejects NaN/Inf at the primary setter.
 * Save-time and bypass entry points still need a finite fallback so Rotation
 * NBT never persists non-finite floats.
 */
public final class EntityRotation {

	private EntityRotation() {
	}

	/**
	 * @return {@code value} when finite; otherwise {@code 0}
	 */
	public static float sanitize(float value) {
		return Float.isFinite(value) ? value : 0.0F;
	}
}
