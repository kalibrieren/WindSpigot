package com.windpvp.windspigot.inventory;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Shape + ingredient matching for shaped crafting recipes.
 * Mirrors NMS {@code ShapedRecipes} placement and mirror rules on a 3×3 grid.
 */
public final class ShapedRecipeMatcher {

	private static final int GRID = 3;
	/** Same wildcard as Bukkit {@code setIngredient(..., -1)} → {@link Short#MAX_VALUE} / NMS 32767. */
	public static final short WILDCARD_DATA = Short.MAX_VALUE;

	private ShapedRecipeMatcher() {
	}

	/**
	 * @param shape       recipe rows (1–3), each 1–3 chars
	 * @param ingredients char → stack (null / missing = empty slot); durability {@link #WILDCARD_DATA} matches any data
	 * @param matrix      length-9 row-major 3×3; null / AIR / typeId ≤ 0 = empty
	 */
	public static boolean matches(String[] shape, Map<Character, ItemStack> ingredients, ItemStack[] matrix) {
		if (shape == null || shape.length == 0 || matrix == null || matrix.length < GRID * GRID) {
			return false;
		}
		int height = shape.length;
		int width = shape[0].length();
		if (width == 0 || height > GRID || width > GRID) {
			return false;
		}
		for (int row = 1; row < height; row++) {
			if (shape[row] == null || shape[row].length() != width) {
				return false;
			}
		}

		for (int offX = 0; offX <= GRID - width; offX++) {
			for (int offY = 0; offY <= GRID - height; offY++) {
				if (matchesAt(shape, ingredients, matrix, width, height, offX, offY, true)
						|| matchesAt(shape, ingredients, matrix, width, height, offX, offY, false)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean matchesAt(String[] shape, Map<Character, ItemStack> ingredients, ItemStack[] matrix,
			int width, int height, int offX, int offY, boolean mirror) {
		for (int gridX = 0; gridX < GRID; gridX++) {
			for (int gridY = 0; gridY < GRID; gridY++) {
				int recipeX = gridX - offX;
				int recipeY = gridY - offY;
				ItemStack expected = null;

				if (recipeX >= 0 && recipeY >= 0 && recipeX < width && recipeY < height) {
					int shapeX = mirror ? (width - recipeX - 1) : recipeX;
					char key = shape[recipeY].charAt(shapeX);
					if (key != ' ' && ingredients != null) {
						expected = ingredients.get(key);
					}
				}

				ItemStack actual = matrix[gridY * GRID + gridX];
				if (!slotMatches(expected, actual)) {
					return false;
				}
			}
		}
		return true;
	}

	static boolean slotMatches(ItemStack expected, ItemStack actual) {
		boolean expectEmpty = isEmpty(expected);
		boolean actualEmpty = isEmpty(actual);
		if (expectEmpty && actualEmpty) {
			return true;
		}
		if (expectEmpty || actualEmpty) {
			return false;
		}
		if (expected.getTypeId() != actual.getTypeId()) {
			return false;
		}
		short data = expected.getDurability();
		return data == WILDCARD_DATA || data == actual.getDurability();
	}

	static boolean isEmpty(ItemStack stack) {
		return stack == null || stack.getTypeId() <= 0 || stack.getType() == Material.AIR;
	}
}
