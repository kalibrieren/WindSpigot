package com.windpvp.windspigot.entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.World;
import org.junit.Assert;
import org.junit.Test;

import net.minecraft.server.NBTTagCompound;

/**
 * Regression for Entity player NBT world reset (TODO #26 / rank 26):
 * resolve by WorldUID only — never by legacy {@code "world"} name.
 */
public class PlayerWorldNbtTest {

	private static NBTTagCompound withWorldUid(UUID uid) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setLong("WorldUUIDMost", uid.getMostSignificantBits());
		nbt.setLong("WorldUUIDLeast", uid.getLeastSignificantBits());
		return nbt;
	}

	private static World worldProxy(final UUID uid, final String name) {
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				String m = method.getName();
				if ("getUID".equals(m)) {
					return uid;
				}
				if ("getName".equals(m)) {
					return name;
				}
				if ("equals".equals(m)) {
					return proxy == args[0];
				}
				if ("hashCode".equals(m)) {
					return uid.hashCode();
				}
				if ("toString".equals(m)) {
					return "WorldProxy(" + name + ")";
				}
				return null;
			}
		};
		return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] { World.class }, handler);
	}

	private static Server serverWithWorlds(final World... worlds) {
		final Map<UUID, World> byUid = new HashMap<UUID, World>();
		final Map<String, World> byName = new HashMap<String, World>();
		for (World w : worlds) {
			byUid.put(w.getUID(), w);
			byName.put(w.getName().toLowerCase(), w);
		}
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				String m = method.getName();
				if ("getWorld".equals(m) && args != null && args.length == 1) {
					if (args[0] instanceof UUID) {
						return byUid.get(args[0]);
					}
					if (args[0] instanceof String) {
						return byName.get(((String) args[0]).toLowerCase());
					}
				}
				if ("equals".equals(m)) {
					return proxy == args[0];
				}
				if ("hashCode".equals(m)) {
					return System.identityHashCode(proxy);
				}
				if ("toString".equals(m)) {
					return "ServerProxy";
				}
				return null;
			}
		};
		return (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] { Server.class },
				handler);
	}

	@Test
	public void readWorldUidFromTags() {
		UUID uid = UUID.fromString("11111111-2222-3333-4444-555555555555");
		Assert.assertEquals(uid, PlayerWorldNbt.readWorldUid(withWorldUid(uid)));
	}

	@Test
	public void readWorldUidNullWhenMissing() {
		Assert.assertNull(PlayerWorldNbt.readWorldUid(new NBTTagCompound()));
		Assert.assertNull(PlayerWorldNbt.readWorldUid(null));
	}

	@Test
	public void readWorldUidIgnoresLegacyWorldName() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("world", "world");
		Assert.assertNull("Legacy name alone must not produce a WorldUID", PlayerWorldNbt.readWorldUid(nbt));
	}

	@Test
	public void resolveUsesWorldUidNotName() {
		UUID uid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
		World expected = worldProxy(uid, "arena");
		World decoy = worldProxy(UUID.fromString("00000000-0000-0000-0000-000000000001"), "world");
		Server server = serverWithWorlds(expected, decoy);

		NBTTagCompound nbt = withWorldUid(uid);
		nbt.setString("world", "world"); // would wrongly pick decoy if name were used

		Assert.assertSame(expected, PlayerWorldNbt.resolve(server, nbt));
	}

	@Test
	public void resolveReturnsNullForNameOnlyNbt() {
		UUID uid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
		World named = worldProxy(uid, "world");
		Server server = serverWithWorlds(named);

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("world", "world");

		Assert.assertNull("Must not fall back to legacy world name", PlayerWorldNbt.resolve(server, nbt));
	}

	@Test
	public void resolveReturnsNullWhenWorldUnloaded() {
		UUID uid = UUID.fromString("11111111-2222-3333-4444-555555555555");
		Server server = serverWithWorlds(); // no worlds loaded
		Assert.assertNull(PlayerWorldNbt.resolve(server, withWorldUid(uid)));
	}
}
