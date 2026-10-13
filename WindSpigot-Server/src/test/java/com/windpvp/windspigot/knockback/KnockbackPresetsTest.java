package com.windpvp.windspigot.knockback;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.cobblesword.nachospigot.knockback.KnockbackProfile;

/**
 * Verifies the finished {@code windpvp} preset is complete and distinct from kohi,
 * and that fresh {@code knockback.yml} seeding persists those values.
 */
public class KnockbackPresetsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void windpvpSetsEveryKnobExplicitly() {
		CraftKnockbackProfile profile = new CraftKnockbackProfile("windpvp");
		KnockbackPresets.applyWindpvp(profile);

		Assert.assertTrue(profile.isStopSprint());
		Assert.assertEquals(2.0D, profile.getFrictionHorizontal(), 0.0D);
		Assert.assertEquals(2.0D, profile.getFrictionVertical(), 0.0D);

		Assert.assertEquals(0.35D, profile.getHorizontal(), 0.0D);
		Assert.assertEquals(0.35D, profile.getVertical(), 0.0D);
		Assert.assertEquals(0.4D, profile.getVerticalMax(), 0.0D);
		Assert.assertEquals(-1.0D, profile.getVerticalMin(), 0.0D);

		Assert.assertEquals(0.425D, profile.getExtraHorizontal(), 0.0D);
		Assert.assertEquals(0.085D, profile.getExtraVertical(), 0.0D);
		Assert.assertEquals(0.425D, profile.getWTapExtraHorizontal(), 0.0D);
		Assert.assertEquals(0.085D, profile.getWTapExtraVertical(), 0.0D);

		Assert.assertEquals(0.0D, profile.getAddHorizontal(), 0.0D);
		Assert.assertEquals(0.0D, profile.getAddVertical(), 0.0D);

		Assert.assertEquals(0.425D, profile.getRodHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, profile.getRodVertical(), 0.0D);
		Assert.assertEquals(0.425D, profile.getArrowHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, profile.getArrowVertical(), 0.0D);
		Assert.assertEquals(0.35D, profile.getPearlHorizontal(), 0.0D);
		Assert.assertEquals(0.35D, profile.getPearlVertical(), 0.0D);
		Assert.assertEquals(0.425D, profile.getSnowballHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, profile.getSnowballVertical(), 0.0D);
		Assert.assertEquals(0.425D, profile.getEggHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, profile.getEggVertical(), 0.0D);
	}

	@Test
	public void windpvpDiffersFromKohiOnProjectiles() {
		CraftKnockbackProfile windpvp = new CraftKnockbackProfile("windpvp");
		CraftKnockbackProfile kohi = new CraftKnockbackProfile("kohi");
		KnockbackPresets.applyWindpvp(windpvp);
		KnockbackPresets.applyKohi(kohi);

		Assert.assertEquals(kohi.getHorizontal(), windpvp.getHorizontal(), 0.0D);
		Assert.assertEquals(kohi.getVertical(), windpvp.getVertical(), 0.0D);
		Assert.assertEquals(kohi.getExtraHorizontal(), windpvp.getExtraHorizontal(), 0.0D);
		Assert.assertEquals(kohi.getExtraVertical(), windpvp.getExtraVertical(), 0.0D);

		// WindSpigot identity: slightly stronger non-pearl projectiles than kohi
		Assert.assertTrue(windpvp.getRodHorizontal() > kohi.getRodHorizontal());
		Assert.assertTrue(windpvp.getArrowHorizontal() > kohi.getArrowHorizontal());
		Assert.assertTrue(windpvp.getSnowballHorizontal() > kohi.getSnowballHorizontal());
		Assert.assertTrue(windpvp.getEggHorizontal() > kohi.getEggHorizontal());
		Assert.assertEquals(kohi.getPearlHorizontal(), windpvp.getPearlHorizontal(), 0.0D);
	}

	@Test
	public void freshKnockbackYmlSeedsCompleteWindpvpProfile() throws IOException {
		KnockbackConfig.clearForTest();
		File configFile = folder.newFile("knockback.yml");

		KnockbackConfig.init(configFile);

		KnockbackProfile loaded = KnockbackConfig.getKbProfileByName("windpvp");
		Assert.assertNotNull("windpvp must be seeded on first boot", loaded);

		Assert.assertTrue(loaded.isStopSprint());
		Assert.assertEquals(2.0D, loaded.getFrictionHorizontal(), 0.0D);
		Assert.assertEquals(2.0D, loaded.getFrictionVertical(), 0.0D);
		Assert.assertEquals(0.35D, loaded.getHorizontal(), 0.0D);
		Assert.assertEquals(0.35D, loaded.getVertical(), 0.0D);
		Assert.assertEquals(0.4D, loaded.getVerticalMax(), 0.0D);
		Assert.assertEquals(-1.0D, loaded.getVerticalMin(), 0.0D);
		Assert.assertEquals(0.425D, loaded.getExtraHorizontal(), 0.0D);
		Assert.assertEquals(0.085D, loaded.getExtraVertical(), 0.0D);
		Assert.assertEquals(0.425D, loaded.getWTapExtraHorizontal(), 0.0D);
		Assert.assertEquals(0.085D, loaded.getWTapExtraVertical(), 0.0D);
		Assert.assertEquals(0.0D, loaded.getAddHorizontal(), 0.0D);
		Assert.assertEquals(0.0D, loaded.getAddVertical(), 0.0D);
		Assert.assertEquals(0.425D, loaded.getRodHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, loaded.getArrowHorizontal(), 0.0D);
		Assert.assertEquals(0.35D, loaded.getPearlHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, loaded.getSnowballHorizontal(), 0.0D);
		Assert.assertEquals(0.425D, loaded.getEggHorizontal(), 0.0D);

		// Runtime default remains kohi (Nacho compat); windpvp is an opt-in preset.
		Assert.assertEquals("kohi", KnockbackConfig.getCurrentKb().getName());
		Assert.assertNotNull(KnockbackConfig.getKbProfileByName("kohi"));
		Assert.assertNotNull(KnockbackConfig.getKbProfileByName("vanilla"));
		Assert.assertNotNull(KnockbackConfig.getKbProfileByName("hypixel"));
	}

	@Test
	public void existingWindpvpSectionIsNotOverwritten() throws IOException {
		KnockbackConfig.clearForTest();
		File configFile = folder.newFile("knockback-existing.yml");

		KnockbackConfig.init(configFile);
		KnockbackProfile first = KnockbackConfig.getKbProfileByName("windpvp");
		Assert.assertNotNull(first);
		first.setHorizontal(0.99D);
		first.save(true);

		KnockbackConfig.clearForTest();
		KnockbackConfig.init(configFile);

		KnockbackProfile second = KnockbackConfig.getKbProfileByName("windpvp");
		Assert.assertNotNull(second);
		Assert.assertEquals("Operator-edited windpvp must persist across reload", 0.99D,
				second.getHorizontal(), 0.0D);
	}
}
