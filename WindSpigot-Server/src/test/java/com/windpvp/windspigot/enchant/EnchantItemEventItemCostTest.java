package com.windpvp.windspigot.enchant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

/**
 * Locks EnchantItemEvent lapis/item-cost API (TODO #20): vanilla default is
 * button+1; plugins may set 0+; negatives rejected.
 * <p>
 * Uses JDK proxies for interfaces; InventoryView is an abstract class in 1.8.
 */
public class EnchantItemEventItemCostTest {

	@SuppressWarnings("unchecked")
	private static <T> T stub(Class<T> type) {
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				Class<?> r = method.getReturnType();
				if (r == boolean.class) {
					return false;
				}
				if (r == int.class || r == byte.class || r == short.class || r == char.class) {
					return 0;
				}
				if (r == long.class) {
					return 0L;
				}
				if (r == float.class) {
					return 0.0F;
				}
				if (r == double.class) {
					return 0.0D;
				}
				if (r == void.class) {
					return null;
				}
				if (method.getName().equals("equals")) {
					return proxy == args[0];
				}
				if (method.getName().equals("hashCode")) {
					return System.identityHashCode(proxy);
				}
				if (method.getName().equals("toString")) {
					return "stub:" + type.getSimpleName();
				}
				return null;
			}
		};
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
	}

	/** Minimal InventoryView — abstract in Bukkit 1.8, so not proxyable. */
	private static InventoryView stubView() {
		final Inventory top = stub(Inventory.class);
		final Inventory bottom = stub(Inventory.class);
		final HumanEntity player = stub(Player.class);
		return new InventoryView() {
			@Override
			public Inventory getTopInventory() {
				return top;
			}

			@Override
			public Inventory getBottomInventory() {
				return bottom;
			}

			@Override
			public HumanEntity getPlayer() {
				return player;
			}

			@Override
			public InventoryType getType() {
				return InventoryType.ENCHANTING;
			}
		};
	}

	private static EnchantItemEvent eventForButton(int button) {
		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		return new EnchantItemEvent(stub(Player.class), stubView(), stub(Block.class),
				new ItemStack(Material.DIAMOND_SWORD), 30, enchants, button);
	}

	@Test
	public void defaultItemCostIsVanillaButtonPlusOne() {
		for (int button = 0; button < 3; button++) {
			EnchantItemEvent event = eventForButton(button);
			assertEquals(button, event.whichButton());
			assertEquals(EnchantLapisCost.vanillaCost(button), event.getItemCost());
		}
	}

	@Test
	public void pluginCanSetFreeOrHigherLapisCost() {
		EnchantItemEvent event = eventForButton(2);
		assertEquals(3, event.getItemCost());

		event.setItemCost(0);
		assertEquals(0, event.getItemCost());

		event.setItemCost(5);
		assertEquals(5, event.getItemCost());
		assertFalse(event.isCancelled());
	}

	@Test
	public void negativeItemCostRejected() {
		EnchantItemEvent event = eventForButton(0);
		try {
			event.setItemCost(-1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertEquals(1, event.getItemCost()); // unchanged
		}
	}

	@Test
	public void emptyEnchantsMapAccepted() {
		EnchantItemEvent event = new EnchantItemEvent(stub(Player.class), stubView(), stub(Block.class),
				new ItemStack(Material.BOOK), 1, Collections.<Enchantment, Integer>emptyMap(), 0);
		assertEquals(1, event.getItemCost());
	}
}
