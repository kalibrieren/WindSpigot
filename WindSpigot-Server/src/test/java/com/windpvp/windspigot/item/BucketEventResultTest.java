package com.windpvp.windspigot.item;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;
import org.junit.Test;

import net.minecraft.server.Items;

/**
 * Regression for ItemBucket Bukkit result handling (TODO #5 / priority #29).
 * <p>
 * PlayerBucket*Event#getItemStack() must become the NMS hand/overflow stack via a
 * single {@link CraftItemStack#asNMSCopy} — the same path EntityCow uses for milking.
 */
public class BucketEventResultTest {

	@Test
	public void defaultFillResultCopiesWaterBucket() {
		ItemStack bukkit = CraftItemStack.asNewCraftStack(Items.WATER_BUCKET);
		net.minecraft.server.ItemStack nms = CraftItemStack.asNMSCopy(bukkit);

		Assert.assertNotNull(nms);
		Assert.assertEquals(Items.WATER_BUCKET, nms.getItem());
		Assert.assertEquals(1, nms.count);
	}

	@Test
	public void pluginClearedResultBecomesNullNmsStack() {
		Assert.assertNull("setItemStack(null) must empty the hand after consume",
				CraftItemStack.asNMSCopy((ItemStack) null));
	}

	@Test
	public void pluginCustomResultPreservesTypeAndAmount() {
		ItemStack custom = new ItemStack(Material.DIAMOND, 3);
		net.minecraft.server.ItemStack nms = CraftItemStack.asNMSCopy(custom);

		Assert.assertNotNull(nms);
		Assert.assertEquals(Items.DIAMOND, nms.getItem());
		Assert.assertEquals(3, nms.count);
	}

	@Test
	public void emptyBucketResultCopiesEmptyBucket() {
		ItemStack bukkit = CraftItemStack.asNewCraftStack(Items.BUCKET);
		net.minecraft.server.ItemStack nms = CraftItemStack.asNMSCopy(bukkit);

		Assert.assertNotNull(nms);
		Assert.assertEquals(Items.BUCKET, nms.getItem());
		Assert.assertEquals(1, nms.count);
	}
}
