package com.windpvp.windspigot.inventory;

/**
 * How {@code HumanEntity#openInventory(InventoryView)} should bind a container.
 * <p>
 * Real players need an {@code EntityPlayer} with a live {@code PlayerConnection}
 * so open/close packets can reach the client. Citizens-style NPCs are often
 * {@code EntityPlayer} with a null connection; other NPC plugins may use a
 * non-player {@code EntityHuman}. Both cases open server-side only (no packets,
 * no slot listener that would NPE on a null connection).
 */
public final class HumanInventoryOpenMode {

	public enum Mode {
		/** Full client open: packets + InventoryOpenEvent via CraftEventFactory. */
		CLIENT,
		/** Bind {@code activeContainer} only; safe for NPCs / offline players. */
		SERVER_SIDE
	}

	private HumanInventoryOpenMode() {
	}

	/**
	 * @param entityPlayer       whether the handle is an {@code EntityPlayer}
	 * @param connectionPresent  whether {@code EntityPlayer.playerConnection != null}
	 */
	public static Mode resolve(boolean entityPlayer, boolean connectionPresent) {
		return entityPlayer && connectionPresent ? Mode.CLIENT : Mode.SERVER_SIDE;
	}
}
