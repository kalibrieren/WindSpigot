package com.windpvp.windspigot.inventory;

/**
 * Policy for {@link org.bukkit.inventory.meta.Repairable} repair-cost penalty
 * on item meta ({@code CraftMetaItem}).
 *
 * <p><b>Does this have limits?</b> No Bukkit/API clamp. Vanilla stores
 * {@code RepairCost} as a plain NBT {@code int} with no range check
 * ({@code ItemStack#setRepairCost}). The anvil “Too Expensive” threshold (40
 * levels in 1.8) caps the <em>XP cost of the current anvil operation</em>, not
 * this persistent item penalty. Non-positive values are treated as absent by
 * {@link #isPresent(int)}.
 */
public final class RepairCostLimits {

	private RepairCostLimits() {
	}

	/**
	 * Whether meta should report a repair cost (same rule as
	 * {@code CraftMetaItem#hasRepairCost()}).
	 */
	public static boolean isPresent(int cost) {
		return cost > 0;
	}

	/**
	 * Apply a plugin/API-supplied cost for storage. Identity — no clamp — for
	 * vanilla NBT parity.
	 */
	public static int apply(int cost) {
		return cost;
	}
}
