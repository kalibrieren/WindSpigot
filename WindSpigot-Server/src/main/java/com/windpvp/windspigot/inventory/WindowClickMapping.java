package com.windpvp.windspigot.inventory;

/**
 * Pure helpers for {@code PacketPlayInWindowClick} / block-place inventory sync
 * decisions in {@code PlayerConnection}. Kept free of NMS so the rules are
 * unit-testable.
 *
 * <p>1.8.8 click modes (frozen protocol):
 * <ul>
 *   <li>0 – pickup (left/right)</li>
 *   <li>1 – quick-move (shift-click)</li>
 *   <li>2 – number-key hotbar swap</li>
 *   <li>3 – creative middle-click clone</li>
 *   <li>4 – drop / control-drop</li>
 *   <li>5 – drag (fires {@code InventoryDragEvent} inside {@code Container})</li>
 *   <li>6 – double-click collect-to-cursor</li>
 * </ul>
 */
public final class WindowClickMapping {

	private WindowClickMapping() {
	}

	/**
	 * Raw slot indexes the client may send for a number-key click. Outside the
	 * container ({@code -999}) and other negatives must not be passed to
	 * {@code Container#getSlot(int)} (throws / clamps incorrectly).
	 */
	public static boolean isValidRawSlot(int rawSlot, int slotCount) {
		return rawSlot >= 0 && rawSlot < slotCount;
	}

	/**
	 * Whether to send {@code PacketPlayOutSetSlot} for the held item after a
	 * block-place / item-use.
	 *
	 * <p>{@code alwaysForce} covers cancelled or throttled places where the
	 * server hand still matches the client's packet stack (so a matches-only
	 * check would skip the resync) while the client already predicted a change.
	 */
	public static boolean shouldResyncHeldSlot(boolean handMatchesPacket, boolean alwaysForce) {
		return !handMatchesPacket || alwaysForce;
	}
}
