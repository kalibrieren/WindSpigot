package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;

/**
 * CraftMetaItem#toString must use the serialized meta-type key (TYPE_FIELD) as
 * its debug prefix — the same value serialize() writes — instead of a separate
 * classMap lookup (legacy "TODO: cry").
 */
public class CraftMetaItemToStringTest {

	@Test
	public void unspecificMetaToStringUsesSerializeKey() {
		ItemMeta meta = CraftItemFactory.instance().getItemMeta(Material.DIAMOND_SWORD);
		meta.setDisplayName("WindBlade");

		Map<String, Object> serialized = meta.serialize();
		String type = (String) serialized.get("meta-type");
		assertEquals("UNSPECIFIC", type);

		String text = meta.toString();
		assertTrue(text.startsWith(type + "_META:"));
		assertTrue(text.contains("display-name=WindBlade"));
		assertTrue(text.contains("meta-type=" + type));
		assertFalse(text.contains("TODO"));
	}

	@Test
	public void typedMetasUseTheirSerializeKeys() {
		assertMetaTypePrefix(Material.POTION, "POTION");
		assertMetaTypePrefix(Material.SKULL_ITEM, "SKULL");
		assertMetaTypePrefix(Material.ENCHANTED_BOOK, "ENCHANTED");
		assertMetaTypePrefix(Material.MAP, "MAP");
		assertMetaTypePrefix(Material.LEATHER_CHESTPLATE, "LEATHER_ARMOR");
		assertMetaTypePrefix(Material.BANNER, "BANNER");
	}

	private static void assertMetaTypePrefix(Material material, String expectedType) {
		ItemMeta meta = CraftItemFactory.instance().getItemMeta(material);
		assertEquals(expectedType, meta.serialize().get("meta-type"));
		assertTrue(meta.toString().startsWith(expectedType + "_META:"));
	}
}
