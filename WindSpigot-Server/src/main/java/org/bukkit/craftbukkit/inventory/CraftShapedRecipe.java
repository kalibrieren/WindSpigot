package org.bukkit.craftbukkit.inventory;

import java.util.Map;

import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import com.windpvp.windspigot.inventory.ShapedRecipeMatcher;

import net.minecraft.server.CraftingManager;
import net.minecraft.server.InventoryCrafting;
import net.minecraft.server.ShapedRecipes;

public class CraftShapedRecipe extends ShapedRecipe implements CraftRecipe {
	/** Backing NMS recipe when wrapped via {@link ShapedRecipes#toBukkitRecipe()}. */
	private ShapedRecipes recipe;

	public CraftShapedRecipe(ItemStack result) {
		super(result);
	}

	public CraftShapedRecipe(ItemStack result, ShapedRecipes recipe) {
		this(result);
		this.recipe = recipe;
	}

	public static CraftShapedRecipe fromBukkitRecipe(ShapedRecipe recipe) {
		if (recipe instanceof CraftShapedRecipe) {
			return (CraftShapedRecipe) recipe;
		}
		CraftShapedRecipe ret = new CraftShapedRecipe(recipe.getResult());
		String[] shape = recipe.getShape();
		ret.shape(shape);
		Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();
		for (char c : ingredientMap.keySet()) {
			ItemStack stack = ingredientMap.get(c);
			if (stack != null) {
				ret.setIngredient(c, stack.getType(), stack.getDurability());
			}
		}
		return ret;
	}

	/**
	 * Whether this recipe matches the given crafting inventory.
	 * Prefers the backing NMS {@link ShapedRecipes} when present; otherwise uses
	 * Bukkit shape/ingredients with the same 3×3 placement and mirror rules.
	 *
	 * @param inventory crafting matrix (3×3)
	 * @return true if the matrix matches this recipe
	 */
	public boolean matches(InventoryCrafting inventory) {
		if (inventory == null) {
			return false;
		}
		if (this.recipe != null) {
			return this.recipe.a(inventory, null);
		}
		return matches(toBukkitMatrix(inventory));
	}

	/**
	 * Whether this recipe matches a 3×3 crafting matrix (row-major, length ≥ 9).
	 * null / AIR slots are empty; ingredient durability {@link Short#MAX_VALUE} is a wildcard.
	 *
	 * @param matrix crafting grid
	 * @return true if the matrix matches this recipe
	 */
	public boolean matches(ItemStack[] matrix) {
		return ShapedRecipeMatcher.matches(getShape(), getIngredientMap(), matrix);
	}

	private static ItemStack[] toBukkitMatrix(InventoryCrafting inventory) {
		ItemStack[] matrix = new ItemStack[9];
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				net.minecraft.server.ItemStack nms = inventory.c(x, y);
				matrix[y * 3 + x] = nms == null ? null : CraftItemStack.asCraftMirror(nms);
			}
		}
		return matrix;
	}

	@Override
	public void addToCraftingManager() {
		Object[] data;
		String[] shape = this.getShape();
		Map<Character, ItemStack> ingred = this.getIngredientMap();
		int datalen = shape.length;
		datalen += ingred.size() * 2;
		int i = 0;
		data = new Object[datalen];
		for (; i < shape.length; i++) {
			data[i] = shape[i];
		}
		for (char c : ingred.keySet()) {
			ItemStack mdata = ingred.get(c);
			if (mdata == null) {
				continue;
			}
			data[i] = c;
			i++;
			int id = mdata.getTypeId();
			short dmg = mdata.getDurability();
			data[i] = new net.minecraft.server.ItemStack(CraftMagicNumbers.getItem(id), 1, dmg);
			i++;
		}
		CraftingManager.getInstance().registerShapedRecipe(CraftItemStack.asNMSCopy(this.getResult()), data);
	}
}
