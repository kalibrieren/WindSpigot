package org.bukkit.craftbukkit;

import org.apache.commons.lang.Validate;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.potion.Potion;

public class CraftEffect {
	public static <T> int getDataValue(Effect effect, T data) {
		int datavalue;
		switch (effect) {
		case POTION_BREAK:
			datavalue = ((Potion) data).toDamageValue() & 0x3F;
			break;
		case RECORD_PLAY:
			Validate.isTrue(((Material) data).isRecord(), "Invalid record type!");
			datavalue = ((Material) data).getId();
			break;
		case SMOKE:
			// Minecraft 1.8 world-event 2000 encodes direction as:
			// data = (dx + 1) + (dz + 1) * 3 where dx, dz ∈ {-1, 0, 1}
			// Matches DispenseBehaviorItem.a(EnumDirection) and the client decode
			// (dx = data % 3 - 1, dz = data / 3 % 3 - 1). Pre-BUKKIT-1567 BlockFace
			// names left the old CraftBukkit table inverted vs current mods.
			datavalue = smokeDataValue((BlockFace) data);
			break;
		case STEP_SOUND:
			Validate.isTrue(((Material) data).isBlock(), "Material is not a block!");
			datavalue = ((Material) data).getId();
			break;
		case ITEM_BREAK:
			datavalue = ((Material) data).getId();
			break;
		default:
			datavalue = 0;
		}
		return datavalue;
	}

	/**
	 * Maps a {@link BlockFace} to the 1.8 smoke effect data byte (0–8).
	 */
	public static int smokeDataValue(BlockFace face) {
		switch (face) {
		case NORTH_WEST:
		case NORTH:
		case NORTH_EAST:
		case WEST:
		case EAST:
		case SOUTH_WEST:
		case SOUTH:
		case SOUTH_EAST:
		case UP:
		case DOWN:
		case SELF:
			return face.getModX() + 1 + (face.getModZ() + 1) * 3;
		default:
			throw new IllegalArgumentException("Bad smoke direction!");
		}
	}
}
