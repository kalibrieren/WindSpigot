package com.windpvp.windspigot.craftbukkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.server.Block;
import net.minecraft.server.Blocks;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.Item;

/**
 * Map-backed CraftMagicNumbers Material↔NMS lookups must match the legacy
 * numeric-ID bridge on every 1.8.8 Material (API parity).
 */
public class CraftMagicNumbersMaterialMapsTest {

	@BeforeClass
	public static void bootstrapNms() {
		DispenserRegistry.c();
	}

	@Test
	public void getItemMatchesLegacyIdLookup() {
		for (Material material : Material.values()) {
			assertSame(material.name(), Item.getById(material.getId()), CraftMagicNumbers.getItem(material));
		}
	}

	@Test
	public void getBlockMatchesLegacyIdLookup() {
		for (Material material : Material.values()) {
			Block legacy = Block.getById(material.getId());
			if (legacy == null) {
				legacy = Blocks.AIR;
			}
			assertSame(material.name(), legacy, CraftMagicNumbers.getBlock(material));
		}
	}

	@Test
	public void getMaterialFromItemMatchesLegacyIdLookup() {
		for (Material material : Material.values()) {
			Item item = Item.getById(material.getId());
			if (item == null) {
				continue;
			}
			Material legacy = Material.getMaterial(Item.getId(item));
			if (legacy == null) {
				legacy = Material.AIR;
			}
			assertEquals(material.name(), legacy, CraftMagicNumbers.getMaterial(item));
		}
	}
}
