package org.bukkit.craftbukkit.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

/**
 * Locks the remove-path optimization. Uses ItemStack subclasses that override
 * isSimilar/equals so tests do not need a live Bukkit ItemFactory.
 */
public class CraftInventoryRemoveOptimizeTest {

	/** Avoids Bukkit.getItemFactory() used by ItemStack#isSimilar / #equals. */
	private static final class KeyedStack extends ItemStack {
		private final int key;

		KeyedStack(int key, int amount) {
			super(Material.STONE, amount);
			this.key = key;
		}

		@Override
		public boolean isSimilar(ItemStack stack) {
			return stack instanceof KeyedStack && ((KeyedStack) stack).key == key;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof KeyedStack)) {
				return false;
			}
			KeyedStack other = (KeyedStack) obj;
			return other.key == key && other.getAmount() == getAmount();
		}
	}

	@Test
	public void firstFromIndexSkipsEarlierMatches() {
		ItemStack needle = new KeyedStack(1, 1);
		ItemStack[] inventory = new ItemStack[] { new KeyedStack(1, 10), new KeyedStack(2, 1), new KeyedStack(1, 5),
				null, new KeyedStack(1, 2) };

		assertEquals(0, CraftInventory.first(needle, false, inventory, 0));
		assertEquals(2, CraftInventory.first(needle, false, inventory, 1));
		assertEquals(2, CraftInventory.first(needle, false, inventory, 2));
		assertEquals(4, CraftInventory.first(needle, false, inventory, 3));
		assertEquals(-1, CraftInventory.first(needle, false, inventory, 5));
	}

	@Test
	public void firstFromIndexRespectsWithAmount() {
		ItemStack needle = new KeyedStack(1, 5);
		ItemStack[] inventory = new ItemStack[] { new KeyedStack(1, 10), new KeyedStack(1, 5) };

		assertEquals(0, CraftInventory.first(needle, false, inventory, 0));
		assertEquals(1, CraftInventory.first(needle, true, inventory, 0));
		assertEquals(-1, CraftInventory.first(needle, true, inventory, 2));
	}

	@Test
	public void removeItemSourceNoLongerCarriesOptimizationTodo() throws Exception {
		Path source = locateCraftInventorySource();
		String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
		int removeIdx = text.indexOf("public HashMap<Integer, ItemStack> removeItem");
		int addIdx = text.indexOf("public HashMap<Integer, ItemStack> addItem");
		assertTrue(removeIdx >= 0);
		assertTrue(addIdx >= 0);

		String removeMethod = text.substring(removeIdx, text.indexOf("private int getMaxItemStack", removeIdx));
		assertFalse("removeItem must not retain // TODO: optimization", removeMethod.contains("TODO: optimization"));
		assertTrue("removeItem should resume via fromIndex/searchFrom",
				removeMethod.contains("searchFrom") && removeMethod.contains("first(item, false, inventory, searchFrom)"));

		String addHeader = text.substring(addIdx, Math.min(text.length(), addIdx + 400));
		assertTrue(addHeader.contains("TODO: some optimization"));
	}

	private static Path locateCraftInventorySource() {
		String[] candidates = {
				"src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java",
				"WindSpigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java",
				"../WindSpigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java" };
		Path cwd = Paths.get("").toAbsolutePath();
		for (String candidate : candidates) {
			Path path = cwd.resolve(candidate).normalize();
			if (Files.isRegularFile(path)) {
				return path;
			}
		}
		Path probe = cwd;
		for (int i = 0; i < 4 && probe != null; i++, probe = probe.getParent()) {
			Path path = probe
					.resolve("WindSpigot-Server/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java");
			if (Files.isRegularFile(path)) {
				return path;
			}
			path = probe.resolve("src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java");
			if (Files.isRegularFile(path)) {
				return path;
			}
		}
		throw new AssertionError("CraftInventory.java not found from cwd=" + cwd);
	}
}
