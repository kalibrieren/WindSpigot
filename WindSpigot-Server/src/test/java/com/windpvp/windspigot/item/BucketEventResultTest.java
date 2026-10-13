package com.windpvp.windspigot.item;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;
import org.junit.Test;

/**
 * Regression for ItemBucket Bukkit result handling (TODO #5 / priority #29).
 * <p>
 * {@code applyBucketEventResult} uses one {@link CraftItemStack#asNMSCopy} of
 * {@code PlayerBucketEvent#getItemStack()}. These cases cover the null-safe edges
 * that path relies on (no Minecraft bootstrap required).
 */
public class BucketEventResultTest {

	@Test
	public void nullEventResultCopiesToNullNmsStack() {
		Assert.assertNull("plugin setItemStack(null) must clear the hand after consume",
				CraftItemStack.asNMSCopy((ItemStack) null));
	}

	@Test
	public void airOrInvalidTypeCopiesToNullNmsStack() {
		Assert.assertNull(CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));
	}
}
