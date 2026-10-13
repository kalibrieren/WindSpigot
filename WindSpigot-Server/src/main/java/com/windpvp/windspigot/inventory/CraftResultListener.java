package com.windpvp.windspigot.inventory;

import java.util.List;

/**
 * Resolves which player should receive craft-result {@code SetSlot} packets.
 * CraftBukkit historically used {@code listeners.get(0)}; that is wrong if
 * another {@code ICrafting} was registered first. Prefer the container owner
 * when they are actually listening.
 */
public final class CraftResultListener {

	private CraftResultListener() {
	}

	/**
	 * @param owner     the human who owns the crafting inventory
	 * @param listeners current container slot listeners ({@code ICrafting}s)
	 * @param type      expected listener type (normally {@code EntityPlayer})
	 * @return the owning listener, or {@code null} if the owner is not that type
	 *         or is not yet registered (e.g. during container construction)
	 */
	public static <T> T findOwningListener(Object owner, List<?> listeners, Class<T> type) {
		if (owner == null || type == null || !type.isInstance(owner) || listeners == null) {
			return null;
		}
		if (!listeners.contains(owner)) {
			return null;
		}
		return type.cast(owner);
	}
}
