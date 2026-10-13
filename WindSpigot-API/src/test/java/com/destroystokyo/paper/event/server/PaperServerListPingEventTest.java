package com.destroystokyo.paper.event.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.util.CachedServerIcon;
import org.junit.BeforeClass;
import org.junit.Test;

import com.destroystokyo.paper.network.StatusClient;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

/**
 * API-level coverage for {@link PaperServerListPingEvent} (Paper status ping
 * surface for WindSpigot 1.8).
 */
public class PaperServerListPingEventTest {

	@BeforeClass
	public static void installStubServer() {
		if (Bukkit.getServer() != null) {
			return;
		}
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				String name = method.getName();
				if ("isPrimaryThread".equals(name)) {
					return Boolean.TRUE;
				}
				if ("getLogger".equals(name)) {
					return Logger.getLogger("PaperServerListPingEventTest");
				}
				if ("getName".equals(name)) {
					return "TestServer";
				}
				if ("getVersion".equals(name) || "getBukkitVersion".equals(name)) {
					return "1.8.8-R0.1-SNAPSHOT";
				}
				if ("getOnlinePlayers".equals(name)) {
					return Collections.emptyList();
				}
				Class<?> type = method.getReturnType();
				if (type == boolean.class) {
					return Boolean.FALSE;
				}
				if (type == int.class) {
					return Integer.valueOf(0);
				}
				if (type == long.class) {
					return Long.valueOf(0L);
				}
				if (type == double.class) {
					return Double.valueOf(0D);
				}
				if (type == float.class) {
					return Float.valueOf(0F);
				}
				if (type == void.class) {
					return null;
				}
				return null;
			}
		};
		Server stub = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] { Server.class },
				handler);
		Bukkit.setServer(stub);
	}

	private static final StatusClient CLIENT = new StatusClient() {
		@Override
		public InetSocketAddress getAddress() {
			return new InetSocketAddress("127.0.0.1", 25565);
		}

		@Override
		public int getProtocolVersion() {
			return 47;
		}

		@Override
		public InetSocketAddress getVirtualHost() {
			return InetSocketAddress.createUnresolved("play.example", 25565);
		}
	};

	private static PaperServerListPingEvent newEvent() {
		return new PaperServerListPingEvent(CLIENT, "A Minecraft Server", 3, 20, "WindSpigot 1.8.8", 47, null);
	}

	@Test
	public void exposesClientMotdCountsAndVersion() {
		PaperServerListPingEvent event = newEvent();
		assertSame(CLIENT, event.getClient());
		assertEquals("A Minecraft Server", event.getMotd());
		assertEquals(3, event.getNumPlayers());
		assertEquals(20, event.getMaxPlayers());
		assertEquals("WindSpigot 1.8.8", event.getVersion());
		assertEquals(47, event.getProtocolVersion());
		assertNull(event.getServerIcon());
		assertFalse(event.isCancelled());
		assertFalse(event.shouldHidePlayers());
	}

	@Test
	public void mutatesMotdCountsVersionProtocolAndCancel() {
		PaperServerListPingEvent event = newEvent();
		event.setMotd("Custom MOTD");
		event.setNumPlayers(99);
		event.setMaxPlayers(200);
		event.setVersion("Custom 1.8");
		event.setProtocolVersion(5);
		event.setCancelled(true);

		assertEquals("Custom MOTD", event.getMotd());
		assertEquals(99, event.getNumPlayers());
		assertEquals(200, event.getMaxPlayers());
		assertEquals("Custom 1.8", event.getVersion());
		assertEquals(5, event.getProtocolVersion());
		assertTrue(event.isCancelled());
	}

	@Test
	public void hidePlayersReturnsSentinelCounts() {
		PaperServerListPingEvent event = newEvent();
		event.setHidePlayers(true);
		assertTrue(event.shouldHidePlayers());
		assertEquals(-1, event.getNumPlayers());
		assertEquals(-1, event.getMaxPlayers());
	}

	@Test
	public void playerSampleIsMutable() {
		PaperServerListPingEvent event = newEvent();
		assertTrue(event.getPlayerSample().isEmpty());

		PlayerProfile profile = new StubProfile(UUID.randomUUID(), "Steve");
		event.getPlayerSample().add(profile);
		assertEquals(1, event.getPlayerSample().size());
		assertEquals("Steve", event.getPlayerSample().get(0).getName());

		event.getPlayerSample().clear();
		assertTrue(event.getPlayerSample().isEmpty());
	}

	@Test
	public void emptyIconIsTreatedAsNull() {
		PaperServerListPingEvent event = newEvent();
		event.setServerIcon(new CachedServerIcon() {
			@Override
			public String getData() {
				return null;
			}
		});
		assertNull(event.getServerIcon());
	}

	@Test
	public void profilePropertyRoundTrip() {
		ProfileProperty property = new ProfileProperty("textures", "value", "sig");
		assertEquals("textures", property.getName());
		assertEquals("value", property.getValue());
		assertEquals("sig", property.getSignature());
		assertTrue(property.isSigned());
	}

	private static final class StubProfile implements PlayerProfile {
		private UUID id;
		private String name;

		StubProfile(UUID id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String setName(String name) {
			String prev = this.name;
			this.name = name;
			return prev;
		}

		@Override
		public UUID getId() {
			return id;
		}

		@Override
		public UUID setId(UUID uuid) {
			UUID prev = this.id;
			this.id = uuid;
			return prev;
		}

		@Override
		public java.util.Set<ProfileProperty> getProperties() {
			return java.util.Collections.emptySet();
		}

		@Override
		public boolean hasProperty(String property) {
			return false;
		}

		@Override
		public void setProperty(ProfileProperty property) {
		}

		@Override
		public void setProperties(java.util.Collection<ProfileProperty> properties) {
		}

		@Override
		public boolean removeProperty(String property) {
			return false;
		}

		@Override
		public void clearProperties() {
		}

		@Override
		public boolean isComplete() {
			return id != null && name != null;
		}

		@Override
		public boolean completeFromCache() {
			return isComplete();
		}

		@Override
		public boolean complete(boolean textures) {
			return isComplete();
		}
	}
}
