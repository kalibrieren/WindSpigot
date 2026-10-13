package com.windpvp.windspigot.entity;

import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.World;

import net.minecraft.server.NBTTagCompound;

/**
 * Resolves a player's Bukkit world from entity NBT using WorldUID only.
 * <p>
 * CraftBukkit historically fell back to a legacy {@code "world"} name string.
 * Saves already write {@code WorldUUIDMost}/{@code WorldUUIDLeast}; load must
 * not depend on renameable world names (multiworld correctness).
 */
public final class PlayerWorldNbt {

	private PlayerWorldNbt() {
	}

	/**
	 * @return world UUID from NBT, or {@code null} when WorldUID tags are absent
	 */
	public static UUID readWorldUid(NBTTagCompound nbt) {
		if (nbt == null) {
			return null;
		}
		if (nbt.hasKey("WorldUUIDMost") && nbt.hasKey("WorldUUIDLeast")) {
			return new UUID(nbt.getLong("WorldUUIDMost"), nbt.getLong("WorldUUIDLeast"));
		}
		return null;
	}

	/**
	 * Resolve via WorldUID only — never via the legacy {@code "world"} name tag.
	 *
	 * @return matching loaded world, or {@code null} if UID missing / world unloaded
	 */
	public static World resolve(Server server, NBTTagCompound nbt) {
		UUID uid = readWorldUid(nbt);
		if (uid == null || server == null) {
			return null;
		}
		return server.getWorld(uid);
	}
}
