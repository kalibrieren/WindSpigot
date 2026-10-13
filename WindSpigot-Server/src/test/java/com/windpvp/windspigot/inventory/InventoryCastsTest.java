package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.InventoryWrapper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

import net.minecraft.server.IInventory;
import net.minecraft.server.PlayerInventory;

/**
 * Type-check helpers for unsafe inventory cast TODOs (beacon/chest/dispenser,
 * CraftInventoryView, CraftContainer).
 */
public class InventoryCastsTest {

	@Test
	public void requirePlayerInventoryAcceptsPlayerInventory() {
		PlayerInventory inv = new PlayerInventory(null);
		assertSame(inv, InventoryCasts.requirePlayerInventory(inv, "test"));
	}

	@Test
	public void requirePlayerInventoryRejectsOtherIInventory() {
		IInventory other = new net.minecraft.server.InventorySubcontainer("test", false, 9);
		try {
			InventoryCasts.requirePlayerInventory(other, "ContainerDispenser");
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("ContainerDispenser"));
			assertTrue(expected.getMessage().contains("PlayerInventory"));
		}
	}

	@Test
	public void requirePlayerInventoryRejectsNull() {
		try {
			InventoryCasts.requirePlayerInventory(null, "ContainerBeacon");
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("null"));
		}
	}

	@Test
	public void resolvePlayerInventoryFallsBackToOpener() {
		PlayerInventory openerInv = new PlayerInventory(null);
		IInventory notPlayer = new net.minecraft.server.InventorySubcontainer("chest", false, 27);
		assertSame(openerInv, InventoryCasts.resolvePlayerInventory(notPlayer, openerInv, "ContainerChest"));
	}

	@Test
	public void resolvePlayerInventoryPrefersExplicitPlayerInventory() {
		PlayerInventory explicit = new PlayerInventory(null);
		PlayerInventory openerInv = new PlayerInventory(null);
		assertSame(explicit, InventoryCasts.resolvePlayerInventory(explicit, openerInv, "ContainerChest"));
	}

	@Test
	public void resolvePlayerInventoryWithoutFallbackThrows() {
		IInventory notPlayer = new net.minecraft.server.InventorySubcontainer("chest", false, 27);
		try {
			InventoryCasts.resolvePlayerInventory(notPlayer, null, "ContainerChest");
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("ContainerChest"));
		}
	}

	@Test
	public void craftInventoryForBeaconUsesPlainCraftInventoryWhenNotTile() {
		IInventory custom = new net.minecraft.server.InventorySubcontainer("beacon", false, 1);
		CraftInventory crafted = InventoryCasts.craftInventoryForBeacon(custom);
		assertSame(custom, crafted.getInventory());
		assertTrue(!(crafted instanceof org.bukkit.craftbukkit.inventory.CraftInventoryBeacon));
	}

	@Test
	public void asNmsInventoryUnwrapsCraftInventory() {
		IInventory nms = new net.minecraft.server.InventorySubcontainer("top", false, 9);
		CraftInventory craft = new CraftInventory(nms);
		assertSame(nms, InventoryCasts.asNmsInventory(craft));
	}

	@Test
	public void asNmsInventoryWrapsCustomBukkitInventory() {
		Inventory custom = new StubBukkitInventory();
		IInventory wrapped = InventoryCasts.asNmsInventory(custom);
		assertTrue(wrapped instanceof InventoryWrapper);
		assertEquals(9, wrapped.getSize());
	}

	@Test
	public void asNmsInventoryRejectsNull() {
		try {
			InventoryCasts.asNmsInventory(null);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertEquals("Inventory cannot be null", expected.getMessage());
		}
	}

	@Test
	public void requireCraftInventoryRejectsNonCraft() {
		try {
			InventoryCasts.requireCraftInventory(new StubBukkitInventory());
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("CraftInventory"));
		}
	}

	@Test
	public void requireCraftHumanEntityRejectsNull() {
		try {
			InventoryCasts.requireCraftHumanEntity(null);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("CraftHumanEntity"));
		}
	}

	private static final class StubBukkitInventory implements Inventory {
		private final ItemStack[] contents = new ItemStack[9];

		@Override
		public int getSize() {
			return contents.length;
		}

		@Override
		public String getName() {
			return "stub";
		}

		@Override
		public ItemStack getItem(int index) {
			return contents[index];
		}

		@Override
		public void setItem(int index, ItemStack item) {
			contents[index] = item;
		}

		@Override
		public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
			return new HashMap<Integer, ItemStack>();
		}

		@Override
		public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
			return new HashMap<Integer, ItemStack>();
		}

		@Override
		public ItemStack[] getContents() {
			return contents;
		}

		@Override
		public void setContents(ItemStack[] items) {
		}

		@Override
		public boolean contains(int materialId) {
			return false;
		}

		@Override
		public boolean contains(Material material) {
			return false;
		}

		@Override
		public boolean contains(ItemStack item) {
			return false;
		}

		@Override
		public boolean contains(int materialId, int amount) {
			return false;
		}

		@Override
		public boolean contains(Material material, int amount) {
			return false;
		}

		@Override
		public boolean contains(ItemStack item, int amount) {
			return false;
		}

		@Override
		public boolean containsAtLeast(ItemStack item, int amount) {
			return false;
		}

		@Override
		public HashMap<Integer, ? extends ItemStack> all(int materialId) {
			return new HashMap<Integer, ItemStack>();
		}

		@Override
		public HashMap<Integer, ? extends ItemStack> all(Material material) {
			return new HashMap<Integer, ItemStack>();
		}

		@Override
		public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
			return new HashMap<Integer, ItemStack>();
		}

		@Override
		public int first(int materialId) {
			return -1;
		}

		@Override
		public int first(Material material) {
			return -1;
		}

		@Override
		public int first(ItemStack item) {
			return -1;
		}

		@Override
		public int firstEmpty() {
			return -1;
		}

		@Override
		public void remove(int materialId) {
		}

		@Override
		public void remove(Material material) {
		}

		@Override
		public void remove(ItemStack item) {
		}

		@Override
		public void clear(int index) {
		}

		@Override
		public void clear() {
		}

		@Override
		public List<HumanEntity> getViewers() {
			return java.util.Collections.emptyList();
		}

		@Override
		public String getTitle() {
			return "stub";
		}

		@Override
		public InventoryType getType() {
			return InventoryType.CHEST;
		}

		@Override
		public InventoryHolder getHolder() {
			return null;
		}

		@Override
		public ListIterator<ItemStack> iterator() {
			return java.util.Arrays.asList(contents).listIterator();
		}

		@Override
		public ListIterator<ItemStack> iterator(int index) {
			return java.util.Arrays.asList(contents).listIterator(index);
		}

		@Override
		public int getMaxStackSize() {
			return 64;
		}

		@Override
		public void setMaxStackSize(int size) {
		}
	}
}
