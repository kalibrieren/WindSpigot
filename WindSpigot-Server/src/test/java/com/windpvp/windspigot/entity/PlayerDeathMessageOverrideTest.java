package com.windpvp.windspigot.entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Regression for EntityPlayer death-message broadcast gate (TODO #19).
 * <p>
 * Previously broadcast required the {@code showDeathMessages} game rule directly,
 * so plugins could not force-show or force-hide without clearing the message text.
 * Paper exposes {@link PlayerDeathEvent#getShowDeathMessages()} /
 * {@link PlayerDeathEvent#setShowDeathMessages(boolean)}; EntityPlayer now honors that.
 * <p>
 * Uses JDK proxies instead of Mockito (1.10.19 is incompatible with modern JDKs).
 */
public class PlayerDeathMessageOverrideTest {

	/** Same broadcast predicate used by EntityPlayer.die after PlayerDeathEvent. */
	private static boolean shouldBroadcast(String deathMessage, boolean showDeathMessages) {
		return deathMessage != null && deathMessage.length() > 0 && showDeathMessages;
	}

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

	@Test
	public void defaultsToShowWhenConstructedWithTrue() {
		Player player = stub(Player.class);
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.<org.bukkit.inventory.ItemStack>emptyList(),
				0, 0, 0, 0, "Player fell from a high place", true);

		Assert.assertTrue(event.getShowDeathMessages());
		Assert.assertTrue(shouldBroadcast(event.getDeathMessage(), event.getShowDeathMessages()));
	}

	@Test
	public void gameruleOffInitializesHidden() {
		Player player = stub(Player.class);
		// CraftEventFactory seeds from showDeathMessages gamerule
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.<org.bukkit.inventory.ItemStack>emptyList(),
				0, 0, 0, 0, "Player fell from a high place", false);

		Assert.assertFalse(event.getShowDeathMessages());
		Assert.assertFalse(shouldBroadcast(event.getDeathMessage(), event.getShowDeathMessages()));
	}

	@Test
	public void pluginCanForceShowWhenGameruleOff() {
		Player player = stub(Player.class);
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.<org.bukkit.inventory.ItemStack>emptyList(),
				0, 0, 0, 0, "Player fell from a high place", false);

		event.setShowDeathMessages(true);
		event.setDeathMessage("Custom death");

		Assert.assertTrue(event.getShowDeathMessages());
		Assert.assertTrue(shouldBroadcast(event.getDeathMessage(), event.getShowDeathMessages()));
	}

	@Test
	public void pluginCanForceHideWhenGameruleOn() {
		Player player = stub(Player.class);
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.<org.bukkit.inventory.ItemStack>emptyList(),
				0, 0, 0, 0, "Player fell from a high place", true);

		event.setShowDeathMessages(false);

		Assert.assertFalse(event.getShowDeathMessages());
		Assert.assertEquals("Player fell from a high place", event.getDeathMessage());
		Assert.assertFalse(shouldBroadcast(event.getDeathMessage(), event.getShowDeathMessages()));
	}

	@Test
	public void emptyDeathMessageStillHides() {
		Player player = stub(Player.class);
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.<org.bukkit.inventory.ItemStack>emptyList(),
				0, "Player fell from a high place");
		event.setDeathMessage("");

		Assert.assertTrue(event.getShowDeathMessages());
		Assert.assertFalse(shouldBroadcast(event.getDeathMessage(), event.getShowDeathMessages()));
	}

	@Test
	public void legacyConstructorDefaultsToShow() {
		Player player = stub(Player.class);
		PlayerDeathEvent event = new PlayerDeathEvent(player, Collections.<org.bukkit.inventory.ItemStack>emptyList(),
				0, "msg");
		Assert.assertTrue(event.getShowDeathMessages());
	}
}
