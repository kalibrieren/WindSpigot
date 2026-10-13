package com.windpvp.windspigot.effect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.bukkit.Effect;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftEffect;
import org.junit.Test;

/**
 * Verifies Effect.SMOKE BlockFace → world-event 2000 data bytes for Minecraft 1.8.
 * <p>
 * Encoding: {@code (modX + 1) + (modZ + 1) * 3}, same as
 * {@code DispenseBehaviorItem} and the client particle offset decode.
 */
public class CraftEffectSmokeDataTest {

	@Test
	public void mapsCardinalsDiagonalsAndVerticalToNmsGrid() {
		assertEquals(0, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.NORTH_WEST));
		assertEquals(1, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.NORTH));
		assertEquals(2, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.NORTH_EAST));
		assertEquals(3, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.WEST));
		assertEquals(4, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.UP));
		assertEquals(4, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.SELF));
		assertEquals(4, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.DOWN));
		assertEquals(5, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.EAST));
		assertEquals(6, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.SOUTH_WEST));
		assertEquals(7, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.SOUTH));
		assertEquals(8, CraftEffect.getDataValue(Effect.SMOKE, BlockFace.SOUTH_EAST));
	}

	@Test
	public void smokeDataValueMatchesDispenseFormula() {
		for (BlockFace face : new BlockFace[] { BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
				BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_EAST,
				BlockFace.UP, BlockFace.DOWN, BlockFace.SELF }) {
			int expected = face.getModX() + 1 + (face.getModZ() + 1) * 3;
			assertEquals(face.name(), expected, CraftEffect.smokeDataValue(face));
		}
	}

	@Test
	public void rejectsNonUnitBlockFaces() {
		try {
			CraftEffect.getDataValue(Effect.SMOKE, BlockFace.NORTH_NORTH_WEST);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}
}
