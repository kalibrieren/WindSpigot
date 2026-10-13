package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

/**
 * Locks the remove-path optimization: {@code first(..., fromIndex)} resumes after
 * a cleared slot instead of rescanning from 0. Add-path TODO is out of scope.
 */
public class CraftInventoryRemoveOptimizeTest {

	@Test
	public void firstFromIndexSkipsEarlierMatches() {
		ItemStack needle = new ItemStack(Material.STONE, 1);
		ItemStack[] inventory = new ItemStack[] { new ItemStack(Material.STONE, 10), new ItemStack(Material.DIRT, 1),
				new ItemStack(Material.STONE, 5), null, new ItemStack(Material.STONE, 2) };

		assertEquals(0, CraftInventory.first(needle, false, inventory, 0));
		assertEquals(2, CraftInventory.first(needle, false, inventory, 1));
		assertEquals(2, CraftInventory.first(needle, false, inventory, 2));
		assertEquals(4, CraftInventory.first(needle, false, inventory, 3));
		assertEquals(-1, CraftInventory.first(needle, false, inventory, 5));
	}

	@Test
	public void firstFromIndexRespectsWithAmount() {
		ItemStack needle = new ItemStack(Material.STONE, 5);
		ItemStack[] inventory = new ItemStack[] { new ItemStack(Material.STONE, 10), new ItemStack(Material.STONE, 5) };

		assertEquals(0, CraftInventory.first(needle, false, inventory, 0)); // isSimilar ignores amount
		assertEquals(1, CraftInventory.first(needle, true, inventory, 0)); // equals requires amount
		assertEquals(-1, CraftInventory.first(needle, true, inventory, 2));
	}

	@Test
	public void removeItemSourceNoLongerCarriesOptimizationTodo() throws Exception {
		Path source = Paths.get("src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java");
		if (!Files.isRegularFile(source)) {
			source = Paths.get("WindSpigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java");
		}
		String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
		int removeIdx = text.indexOf("public HashMap<Integer, ItemStack> removeItem");
		int addIdx = text.indexOf("public HashMap<Integer, ItemStack> addItem");
		assertFalse(removeIdx < 0);
		assertFalse(addIdx < 0);

		String removeMethod = text.substring(removeIdx, text.indexOf("private int getMaxItemStack", removeIdx));
		assertFalse("removeItem must not retain // TODO: optimization", removeMethod.contains("TODO: optimization"));

		// Sibling scope: add-path optimization TODO must remain.
		String addHeader = text.substring(addIdx, Math.min(text.length(), addIdx + 400));
		org.junit.Assert.assertTrue(addHeader.contains("TODO: some optimization"));
	}
}
