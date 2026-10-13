package com.windpvp.windspigot.enchant;

/**
 * Pure helpers for enchantment-table lapis (cost-item) consumption.
 * Vanilla uses button index + 1; plugins may override via
 * {@link org.bukkit.event.enchantment.EnchantItemEvent#setItemCost(int)}.
 */
public final class EnchantLapisCost {

	private EnchantLapisCost() {
	}

	/**
	 * Vanilla lapis cost for the selected offer button (0, 1, or 2) → 1, 2, or 3.
	 */
	public static int vanillaCost(int buttonIndex) {
		return buttonIndex + 1;
	}

	/**
	 * Whether the enchanter can afford {@code itemCost} lapis from the cost slot.
	 * Creative / instant-build always succeeds. A cost of {@code 0} is free.
	 */
	public static boolean canAfford(boolean creativeInstantBuild, int lapisAvailable, int itemCost) {
		return creativeInstantBuild || itemCost <= 0 || lapisAvailable >= itemCost;
	}

	/**
	 * Stack count remaining after consuming {@code itemCost} from {@code lapisAvailable}.
	 * Caller clears the slot when the result is {@code <= 0}.
	 */
	public static int remainingAfterConsume(int lapisAvailable, int itemCost) {
		return lapisAvailable - itemCost;
	}
}
