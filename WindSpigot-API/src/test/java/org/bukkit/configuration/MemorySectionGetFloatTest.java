package org.bukkit.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

/**
 * Regression: YAML stores floating-point scalars as {@link Double}.
 * {@code getFloat} must accept any {@link Number}, not only boxed {@link Float},
 * otherwise Paper/world float settings always fall back to the supplied default.
 */
public class MemorySectionGetFloatTest {

	@Test
	public void getFloatReadsYamlDoubleNotOnlyBoxedFloat() throws Exception {
		YamlConfiguration config = new YamlConfiguration();
		config.loadFromString("player-blocking-damage-multiplier: 0.25\n");

		Object raw = config.get("player-blocking-damage-multiplier");
		assertTrue("SnakeYAML should deserialize decimals as Double", raw instanceof Double);
		assertFalse(raw instanceof Float);

		assertEquals(0.25F, config.getFloat("player-blocking-damage-multiplier", 0.5F), 0.0001F);
		assertEquals(0.25F, config.getFloat("player-blocking-damage-multiplier"), 0.0001F);
	}

	@Test
	public void getFloatUsesExplicitDefaultWhenPathMissing() {
		YamlConfiguration config = new YamlConfiguration();
		assertEquals(0.5F, config.getFloat("missing.path", 0.5F), 0.0001F);
	}

	@Test
	public void getFloatUsesConfigurationDefaultWhenPresentAsDouble() {
		YamlConfiguration config = new YamlConfiguration();
		config.addDefault("block-break", 0.025D);
		config.options().copyDefaults(true);

		// Single-arg form resolves Configuration defaults (Double-boxed).
		assertEquals(0.025F, config.getFloat("block-break"), 0.0001F);
		// Paper/Spigot helper pattern: outer explicit def is the resolved default.
		assertEquals(0.025F, config.getFloat("block-break", config.getFloat("block-break")), 0.0001F);
	}

	@Test
	public void paperWorldSettingsPathPatternReadsOverriddenFloat() throws Exception {
		// Mirrors PaperSpigotWorldConfig.getFloat world-settings.<world>.<path> fallback.
		YamlConfiguration config = new YamlConfiguration();
		config.loadFromString(
				"world-settings:\n"
						+ "  default:\n"
						+ "    player-blocking-damage-multiplier: 0.5\n"
						+ "    player-exhaustion:\n"
						+ "      block-break: 0.025\n"
						+ "      swimming: 0.015\n"
						+ "    tnt-explosion-volume: 4.0\n"
						+ "  world:\n"
						+ "    player-blocking-damage-multiplier: 0.75\n"
						+ "    player-exhaustion:\n"
						+ "      block-break: 0.05\n"
						+ "      swimming: 0.03\n"
						+ "    tnt-explosion-volume: 2.5\n");

		String world = "world";
		assertEquals(0.75F,
				config.getFloat("world-settings." + world + ".player-blocking-damage-multiplier",
						config.getFloat("world-settings.default.player-blocking-damage-multiplier")),
				0.0001F);
		assertEquals(0.05F,
				config.getFloat("world-settings." + world + ".player-exhaustion.block-break",
						config.getFloat("world-settings.default.player-exhaustion.block-break")),
				0.0001F);
		assertEquals(0.03F,
				config.getFloat("world-settings." + world + ".player-exhaustion.swimming",
						config.getFloat("world-settings.default.player-exhaustion.swimming")),
				0.0001F);
		assertEquals(2.5F,
				config.getFloat("world-settings." + world + ".tnt-explosion-volume",
						config.getFloat("world-settings.default.tnt-explosion-volume")),
				0.0001F);
	}

	@Test
	public void paperWorldSettingsFallsBackToDefaultWhenWorldUnset() throws Exception {
		YamlConfiguration config = new YamlConfiguration();
		config.loadFromString(
				"world-settings:\n"
						+ "  default:\n"
						+ "    tnt-explosion-volume: 4.0\n");

		assertEquals(4.0F,
				config.getFloat("world-settings.other.tnt-explosion-volume",
						config.getFloat("world-settings.default.tnt-explosion-volume")),
				0.0001F);
	}

	@Test
	public void getFloatStillAcceptsBoxedFloat() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("boxed", 0.125F);
		assertTrue(config.get("boxed") instanceof Float);
		assertEquals(0.125F, config.getFloat("boxed", 1.0F), 0.0001F);
	}
}
