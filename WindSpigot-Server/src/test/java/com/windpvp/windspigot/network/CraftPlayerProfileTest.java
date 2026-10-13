package com.windpvp.windspigot.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;

/**
 * Round-trip coverage for ping-sample profile conversion.
 */
public class CraftPlayerProfileTest {

	@Test
	public void asBukkitCopyAndAsAuthlibPreserveIdentity() {
		UUID id = UUID.fromString("12345678-1234-1234-1234-123456789abc");
		GameProfile gameProfile = new GameProfile(id, "Alex");
		PlayerProfile profile = CraftPlayerProfile.asBukkitCopy(gameProfile);

		assertEquals(id, profile.getId());
		assertEquals("Alex", profile.getName());
		assertTrue(profile.isComplete());

		GameProfile back = CraftPlayerProfile.asAuthlib(profile);
		assertEquals(id, back.getId());
		assertEquals("Alex", back.getName());
	}

	@Test
	public void propertiesSurviveAuthlibRoundTrip() {
		UUID id = UUID.randomUUID();
		PlayerProfile profile = new CraftPlayerProfile(id, "Steve");
		profile.setProperty(new ProfileProperty("textures", "abc", "sig"));

		GameProfile gameProfile = CraftPlayerProfile.asAuthlib(profile);
		PlayerProfile copy = CraftPlayerProfile.asBukkitCopy(gameProfile);

		assertTrue(copy.hasProperty("textures"));
		ProfileProperty property = copy.getProperties().iterator().next();
		assertEquals("textures", property.getName());
		assertEquals("abc", property.getValue());
		assertEquals("sig", property.getSignature());
	}

	@Test
	public void incompleteProfilesAreAllowed() {
		PlayerProfile nameOnly = new CraftPlayerProfile(null, "Line");
		assertNull(nameOnly.getId());
		assertEquals("Line", nameOnly.getName());
		assertFalse(nameOnly.isComplete());
	}
}
