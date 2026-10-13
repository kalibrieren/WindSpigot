package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

/**
 * Bootstrap-free coverage for shaped recipe placement / mirror / wildcard matching.
 */
public class ShapedRecipeMatcherTest {

	@Test
	public void matchesExactTwoHighStickRecipeAtOrigin() {
		String[] shape = { "A", "A" };
		Map<Character, ItemStack> ingredients = map('A', Material.WOOD);
		ItemStack[] matrix = emptyMatrix();
		matrix[0] = new ItemStack(Material.WOOD);
		matrix[3] = new ItemStack(Material.WOOD);
		assertTrue(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void matchesWhenShiftedWithinGrid() {
		String[] shape = { "A", "A" };
		Map<Character, ItemStack> ingredients = map('A', Material.WOOD);
		ItemStack[] matrix = emptyMatrix();
		matrix[2] = new ItemStack(Material.WOOD);
		matrix[5] = new ItemStack(Material.WOOD);
		assertTrue(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void matchesMirroredHorizontalShape() {
		// Shape "AB" / "  " style: pickaxe head fragment — A left, B right; mirror places B left, A right
		String[] shape = { "AB" };
		Map<Character, ItemStack> ingredients = new HashMap<Character, ItemStack>();
		ingredients.put('A', new ItemStack(Material.COBBLESTONE));
		ingredients.put('B', new ItemStack(Material.STICK));
		ItemStack[] matrix = emptyMatrix();
		matrix[0] = new ItemStack(Material.STICK);
		matrix[1] = new ItemStack(Material.COBBLESTONE);
		assertTrue(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void rejectsExtraItemsOutsideShape() {
		String[] shape = { "A" };
		Map<Character, ItemStack> ingredients = map('A', Material.WOOD);
		ItemStack[] matrix = emptyMatrix();
		matrix[0] = new ItemStack(Material.WOOD);
		matrix[1] = new ItemStack(Material.STICK);
		assertFalse(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void rejectsWrongMaterial() {
		String[] shape = { "A", "A" };
		Map<Character, ItemStack> ingredients = map('A', Material.WOOD);
		ItemStack[] matrix = emptyMatrix();
		matrix[0] = new ItemStack(Material.COBBLESTONE);
		matrix[3] = new ItemStack(Material.WOOD);
		assertFalse(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void wildcardDurabilityMatchesAnyData() {
		String[] shape = { "A" };
		Map<Character, ItemStack> ingredients = new HashMap<Character, ItemStack>();
		ingredients.put('A', new ItemStack(Material.WOOD, 1, ShapedRecipeMatcher.WILDCARD_DATA));
		ItemStack[] matrix = emptyMatrix();
		matrix[4] = new ItemStack(Material.WOOD, 1, (short) 2); // birch planks
		assertTrue(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void specificDurabilityRejectsMismatch() {
		String[] shape = { "A" };
		Map<Character, ItemStack> ingredients = new HashMap<Character, ItemStack>();
		ingredients.put('A', new ItemStack(Material.WOOD, 1, (short) 1));
		ItemStack[] matrix = emptyMatrix();
		matrix[0] = new ItemStack(Material.WOOD, 1, (short) 2);
		assertFalse(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	@Test
	public void spaceInShapeIsEmptySlot() {
		String[] shape = { "A A" };
		Map<Character, ItemStack> ingredients = map('A', Material.IRON_INGOT);
		ItemStack[] matrix = emptyMatrix();
		matrix[0] = new ItemStack(Material.IRON_INGOT);
		matrix[2] = new ItemStack(Material.IRON_INGOT);
		assertTrue(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
		matrix[1] = new ItemStack(Material.STICK);
		assertFalse(ShapedRecipeMatcher.matches(shape, ingredients, matrix));
	}

	private static Map<Character, ItemStack> map(char key, Material material) {
		Map<Character, ItemStack> ingredients = new HashMap<Character, ItemStack>();
		ingredients.put(key, new ItemStack(material));
		return ingredients;
	}

	private static ItemStack[] emptyMatrix() {
		return new ItemStack[9];
	}
}
