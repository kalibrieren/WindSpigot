package com.windpvp.windspigot.registry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.junit.Assert;
import org.junit.Test;

import net.minecraft.server.MobEffectList;

/**
 * Verifies CraftServer's enchant/potion bootstrap: NMS ensure methods register
 * Bukkit types, then freeze rejects further registrations (plugin contract).
 */
public class BukkitRegistrationBootstrapTest {

	@Test
	public void ensureBukkitRegistrationLoadsVanillaEnchantmentsAndPotions() {
		net.minecraft.server.Enchantment.ensureBukkitRegistration();
		MobEffectList.ensureBukkitRegistration();

		Assert.assertNotNull("Sharpness (DAMAGE_ALL) must be registered", Enchantment.getById(16));
		Assert.assertNotNull("Protection must be registered", Enchantment.getById(0));
		Assert.assertNotNull("Blindness must be registered", PotionEffectType.getById(15));
		Assert.assertNotNull("Speed must be registered", PotionEffectType.getById(1));

		Assert.assertSame(Enchantment.getById(16), Enchantment.getByName("DAMAGE_ALL"));
		Assert.assertEquals("BLINDNESS", PotionEffectType.getById(15).getName());
	}

	@Test
	public void freezeRejectsFurtherEnchantmentAndPotionRegistrations() {
		net.minecraft.server.Enchantment.ensureBukkitRegistration();
		MobEffectList.ensureBukkitRegistration();

		if (Enchantment.isAcceptingRegistrations()) {
			Enchantment.stopAcceptingRegistrations();
		}
		PotionEffectType.stopAcceptingRegistrations();

		try {
			Enchantment.registerEnchantment(new FakeEnchantment(200));
			Assert.fail("Expected IllegalStateException after enchantment freeze");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("No longer accepting"));
		}

		try {
			// id 0 is unused in 1.8 (byId length 24); must be in-range to reach freeze check
			PotionEffectType.registerPotionEffectType(new FakePotionEffectType(0));
			Assert.fail("Expected IllegalStateException after potion freeze");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("No longer accepting"));
		}
	}

	private static final class FakeEnchantment extends Enchantment {
		FakeEnchantment(int id) {
			super(id);
		}

		@Override
		public String getName() {
			return "FAKE_ENCHANT_" + getId();
		}

		@Override
		public int getMaxLevel() {
			return 1;
		}

		@Override
		public int getStartLevel() {
			return 1;
		}

		@Override
		public EnchantmentTarget getItemTarget() {
			return EnchantmentTarget.ALL;
		}

		@Override
		public boolean conflictsWith(Enchantment other) {
			return false;
		}

		@Override
		public boolean canEnchantItem(ItemStack item) {
			return false;
		}
	}

	private static final class FakePotionEffectType extends PotionEffectType {
		FakePotionEffectType(int id) {
			super(id);
		}

		@Override
		public double getDurationModifier() {
			return 1.0D;
		}

		@Override
		public String getName() {
			return "FAKE_POTION_" + getId();
		}

		@Override
		public boolean isInstant() {
			return false;
		}
	}
}
