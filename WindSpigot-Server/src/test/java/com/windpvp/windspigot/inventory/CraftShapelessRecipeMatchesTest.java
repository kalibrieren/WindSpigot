package com.windpvp.windspigot.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CraftShapelessRecipe.matches() — order-independent ingredient matching with
 * Short.MAX_VALUE durability as the legacy any-data wildcard.
 */
public class CraftShapelessRecipeMatchesTest {

	@BeforeClass
	public static void stubBukkitItemFactory() {
		if (Bukkit.getServer() != null) {
			return;
		}
		final Logger logger = Logger.getLogger("CraftShapelessRecipeMatchesTest");
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				String name = method.getName();
				if ("getItemFactory".equals(name)) {
					return CraftItemFactory.instance();
				}
				if ("getLogger".equals(name)) {
					return logger;
				}
				if ("getName".equals(name) || "getVersion".equals(name) || "getBukkitVersion".equals(name)) {
					return "WindSpigot-Test";
				}
				Class<?> type = method.getReturnType();
				if (type == boolean.class) {
					return false;
				}
				if (type == int.class) {
					return 0;
				}
				if (type == long.class) {
					return 0L;
				}
				if (type == double.class) {
					return 0.0D;
				}
				if (type == float.class) {
					return 0.0F;
				}
				if (type == void.class) {
					return null;
				}
				return null;
			}
		};
		Server server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] { Server.class },
				handler);
		Bukkit.setServer(server);
	}

	@Test
	public void matchesSameIngredientsAnyOrder() {
		CraftShapelessRecipe recipe = flintAndSteelRecipe();
		assertTrue(recipe.matches(new ItemStack[] { iron(), flint(), null, null }));
		assertTrue(recipe.matches(new ItemStack[] { flint(), null, iron(), null }));
		assertTrue(recipe.matches(new ItemStack[] { null, null, null, flint(), iron() }));
	}

	@Test
	public void rejectsMissingOrExtraIngredients() {
		CraftShapelessRecipe recipe = flintAndSteelRecipe();
		assertFalse(recipe.matches(new ItemStack[] { flint() }));
		assertFalse(recipe.matches(new ItemStack[] { flint(), iron(), new ItemStack(Material.STICK) }));
		assertFalse(recipe.matches(new ItemStack[] { flint(), new ItemStack(Material.STICK) }));
		assertFalse(recipe.matches(new ItemStack[0]));
		assertFalse(recipe.matches((ItemStack[]) null));
		assertFalse(recipe.matches((org.bukkit.inventory.CraftingInventory) null));
	}

	@Test
	public void wildcardDurabilityMatchesAnyData() {
		CraftShapelessRecipe recipe = new CraftShapelessRecipe(new ItemStack(Material.WOOL));
		recipe.addIngredient(Material.INK_SACK, -1); // maps to Short.MAX_VALUE
		assertTrue(recipe.matches(new ItemStack[] { new ItemStack(Material.INK_SACK, 1, (short) 0) }));
		assertTrue(recipe.matches(new ItemStack[] { new ItemStack(Material.INK_SACK, 1, (short) 14) }));
		assertFalse(recipe.matches(new ItemStack[] { new ItemStack(Material.WOOL, 1, (short) 14) }));
	}

	@Test
	public void exactDurabilityDoesNotMatchOtherData() {
		CraftShapelessRecipe recipe = new CraftShapelessRecipe(new ItemStack(Material.WOOL, 1, (short) 14));
		recipe.addIngredient(Material.INK_SACK, 14);
		assertTrue(recipe.matches(new ItemStack[] { new ItemStack(Material.INK_SACK, 1, (short) 14) }));
		assertFalse(recipe.matches(new ItemStack[] { new ItemStack(Material.INK_SACK, 1, (short) 0) }));
	}

	@Test
	public void duplicateIngredientsRequireMatchingCounts() {
		CraftShapelessRecipe recipe = new CraftShapelessRecipe(new ItemStack(Material.BOOK));
		recipe.addIngredient(3, Material.PAPER);
		assertTrue(recipe.matches(new ItemStack[] { paper(), paper(), paper() }));
		assertFalse(recipe.matches(new ItemStack[] { paper(), paper() }));
		assertFalse(recipe.matches(new ItemStack[] { paper(), paper(), paper(), paper() }));
	}

	private static CraftShapelessRecipe flintAndSteelRecipe() {
		CraftShapelessRecipe recipe = new CraftShapelessRecipe(new ItemStack(Material.FLINT_AND_STEEL));
		recipe.addIngredient(Material.IRON_INGOT);
		recipe.addIngredient(Material.FLINT);
		return recipe;
	}

	private static ItemStack flint() {
		return new ItemStack(Material.FLINT);
	}

	private static ItemStack iron() {
		return new ItemStack(Material.IRON_INGOT);
	}

	private static ItemStack paper() {
		return new ItemStack(Material.PAPER);
	}
}
