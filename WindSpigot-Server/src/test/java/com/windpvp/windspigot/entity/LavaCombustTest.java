package com.windpvp.windspigot.entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Regression for Entity lava combust TODOs (spam + null combuster).
 * <p>
 * Matches CraftBukkit / Paper: fire {@link EntityCombustByBlockEvent} only on first
 * ignition for living entities, and pass a real lava block when known.
 */
public class LavaCombustTest {

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
				if (method.getName().equals("getType") && r == Material.class) {
					return Material.LAVA;
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

	@Test
	public void firesOnlyOnFirstIgnitionForLiving() {
		Assert.assertTrue(LavaCombust.shouldCallCombustEvent(true, 0));
		Assert.assertTrue(LavaCombust.shouldCallCombustEvent(true, -20));
		Assert.assertFalse("already burning must not spam combust", LavaCombust.shouldCallCombustEvent(true, 1));
		Assert.assertFalse(LavaCombust.shouldCallCombustEvent(true, 300));
	}

	@Test
	public void neverFiresForNonLiving() {
		Assert.assertFalse(LavaCombust.shouldCallCombustEvent(false, 0));
		Assert.assertFalse(LavaCombust.shouldCallCombustEvent(false, -1));
		Assert.assertFalse(LavaCombust.shouldCallCombustEvent(false, 15));
	}

	@Test
	public void eventCarriesRealCombusterBlock() {
		Block lava = stub(Block.class);
		Entity combustee = stub(Entity.class);

		EntityCombustEvent event = LavaCombust.createEvent(lava, combustee, 15);

		Assert.assertTrue(event instanceof EntityCombustByBlockEvent);
		Assert.assertSame(lava, ((EntityCombustByBlockEvent) event).getCombuster());
		Assert.assertEquals(15, event.getDuration());
		Assert.assertFalse(event.isCancelled());
	}

	@Test
	public void cancelledEventKeepsPluginDurationOverridePath() {
		Block lava = stub(Block.class);
		Entity combustee = stub(Entity.class);

		EntityCombustEvent event = LavaCombust.createEvent(lava, combustee, 15);
		event.setDuration(7);
		event.setCancelled(true);

		Assert.assertTrue(event.isCancelled());
		Assert.assertEquals(7, event.getDuration());
		Assert.assertNotNull(((EntityCombustByBlockEvent) event).getCombuster());
	}
}
