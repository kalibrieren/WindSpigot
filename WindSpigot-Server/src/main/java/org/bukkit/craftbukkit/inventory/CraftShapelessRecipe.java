package org.bukkit.craftbukkit.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import net.minecraft.server.CraftingManager;
import net.minecraft.server.InventoryCrafting;
import net.minecraft.server.ShapelessRecipes;

public class CraftShapelessRecipe extends ShapelessRecipe implements CraftRecipe {
	/** Backing NMS recipe when built from vanilla / registered into CraftingManager. */
	private ShapelessRecipes recipe;

	public CraftShapelessRecipe(ItemStack result) {
		super(result);
	}

	public CraftShapelessRecipe(ItemStack result, ShapelessRecipes recipe) {
		this(result);
		this.recipe = recipe;
	}

	public static CraftShapelessRecipe fromBukkitRecipe(ShapelessRecipe recipe) {
		if (recipe instanceof CraftShapelessRecipe) {
			return (CraftShapelessRecipe) recipe;
		}
		CraftShapelessRecipe ret = new CraftShapelessRecipe(recipe.getResult());
		for (ItemStack ingred : recipe.getIngredientList()) {
			ret.addIngredient(ingred.getType(), ingred.getDurability());
		}
		return ret;
	}

	/**
	 * Check whether this shapeless recipe matches the crafting inventory matrix.
	 * Uses the backing NMS {@link ShapelessRecipes} when present; otherwise matches
	 * the Bukkit ingredient list against {@link CraftingInventory#getMatrix()}.
	 *
	 * @param inventory crafting inventory to test (may be {@code null})
	 * @return {@code true} if the matrix matches this recipe
	 */
	public boolean matches(CraftingInventory inventory) {
		if (inventory == null) {
			return false;
		}
		if (this.recipe != null && inventory instanceof CraftInventoryCrafting) {
			InventoryCrafting matrix = (InventoryCrafting) ((CraftInventoryCrafting) inventory).getMatrixInventory();
			// ShapelessRecipes.a ignores World; null is fine for ingredient matching.
			return this.recipe.a(matrix, null);
		}
		return matches(inventory.getMatrix());
	}

	/**
	 * Check whether the given crafting matrix matches this recipe's ingredients.
	 * Slot order does not matter; empty / air slots are ignored. Ingredient
	 * durability of {@link Short#MAX_VALUE} matches any data value (legacy
	 * wildcard from {@code addIngredient(..., -1)}).
	 *
	 * @param matrix crafting matrix slots (may be {@code null})
	 * @return {@code true} if every non-empty slot matches one remaining ingredient
	 *         and no ingredients are left over
	 */
	public boolean matches(ItemStack[] matrix) {
		if (matrix == null) {
			return false;
		}
		List<ItemStack> remaining = new ArrayList<ItemStack>(getIngredientList());
		for (ItemStack slot : matrix) {
			if (slot == null || slot.getTypeId() == 0) {
				continue;
			}
			boolean found = false;
			Iterator<ItemStack> it = remaining.iterator();
			while (it.hasNext()) {
				ItemStack ingredient = it.next();
				if (ingredient.getTypeId() == slot.getTypeId() && (ingredient.getDurability() == Short.MAX_VALUE
						|| ingredient.getDurability() == slot.getDurability())) {
					it.remove();
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return remaining.isEmpty();
	}

	@Override
	public void addToCraftingManager() {
		List<ItemStack> ingred = this.getIngredientList();
		List<net.minecraft.server.ItemStack> nmsIngredients = new ArrayList<net.minecraft.server.ItemStack>(
				ingred.size());
		for (ItemStack mdata : ingred) {
			int id = mdata.getTypeId();
			short dmg = mdata.getDurability();
			nmsIngredients.add(new net.minecraft.server.ItemStack(CraftMagicNumbers.getItem(id), 1, dmg));
		}
		this.recipe = new ShapelessRecipes(CraftItemStack.asNMSCopy(this.getResult()), nmsIngredients);
		CraftingManager.getInstance().a(this.recipe);
	}
}
