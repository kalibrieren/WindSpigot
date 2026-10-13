package com.windpvp.windspigot.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Locks CraftBukkit CLI logging-option resolution used by DedicatedServer.
 */
public class CommandLineLoggingTest {

	@Test
	public void defaultsDoNotReconfigureFileAppender() {
		OptionSet options = parse();
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(options);

		assertFalse(settings.reconfigureFileAppender);
		assertEquals(CommandLineLogging.DEFAULT_FILE_NAME, settings.fileName);
		assertEquals(CommandLineLogging.DEFAULT_FILE_PATTERN, settings.rolloverPattern);
		assertEquals(0, settings.limitBytes);
		assertEquals(1, settings.count);
		assertTrue(settings.append);
		assertFalse(settings.stripColor);
		assertEquals(CommandLineLogging.DEFAULT_DATE_PATTERN, settings.datePattern);
		assertEquals(CommandLineLogging.DEFAULT_FILE_NAME, settings.julFilePattern());
	}

	@Test
	public void nullOptionsSafeDefaults() {
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(null);
		assertFalse(settings.reconfigureFileAppender);
		assertEquals(CommandLineLogging.DEFAULT_FILE_NAME, settings.fileName);
	}

	@Test
	public void logPatternOverridesFileName() {
		OptionSet options = parse("--log-pattern", "server.log");
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(options);

		assertTrue(settings.reconfigureFileAppender);
		assertEquals("server.log", settings.fileName);
		assertEquals("server.log.%i", settings.rolloverPattern);
		assertEquals("server.log", settings.julFilePattern());
		assertTrue(settings.append);
	}

	@Test
	public void logLimitAndCountEnableRotationPattern() {
		OptionSet options = parse("--log-pattern", "logs/custom.log", "--log-limit", "1024", "--log-count", "3",
				"--log-append", "false");
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(options);

		assertTrue(settings.reconfigureFileAppender);
		assertEquals("logs/custom.log", settings.fileName);
		assertEquals(1024, settings.limitBytes);
		assertEquals(3, settings.count);
		assertFalse(settings.append);
		assertEquals("logs/custom.log.%g", settings.julFilePattern());
	}

	@Test
	public void stripColorFlagsReconfigure() {
		OptionSet options = parse("--log-strip-color");
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(options);

		assertTrue(settings.reconfigureFileAppender);
		assertTrue(settings.stripColor);
		assertTrue(settings.fileLayoutPattern().contains("%msg"));
	}

	@Test
	public void dateFormatAppliedToLayout() {
		OptionSet options = parse("--date-format", "yyyy-MM-dd HH:mm:ss");
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(options);

		assertTrue(settings.reconfigureFileAppender);
		assertEquals("yyyy-MM-dd HH:mm:ss", settings.datePattern);
		assertTrue(settings.fileLayoutPattern().contains("%d{yyyy-MM-dd HH:mm:ss}"));
		assertEquals(CommandLineLogging.DEFAULT_FILE_NAME, settings.fileName);
	}

	@Test
	public void countBelowOneClamped() {
		OptionSet options = parse("--log-count", "0");
		CommandLineLogging.Settings settings = CommandLineLogging.resolve(options);
		assertEquals(1, settings.count);
		assertTrue(settings.reconfigureFileAppender);
	}

	/**
	 * Mirror the logging-related accepts from {@code org.bukkit.craftbukkit.Main}.
	 */
	private static OptionSet parse(String... args) {
		OptionParser parser = new OptionParser() {
			{
				acceptsAll(asList("d", "date-format"), "Format of the date to display in the console (for log entries)")
						.withRequiredArg().ofType(SimpleDateFormat.class).describedAs("Log date format");

				acceptsAll(asList("log-pattern"), "Specfies the log filename pattern").withRequiredArg()
						.ofType(String.class).defaultsTo("server.log").describedAs("Log filename");

				acceptsAll(asList("log-limit"), "Limits the maximum size of the log file (0 = unlimited)")
						.withRequiredArg().ofType(Integer.class).defaultsTo(0).describedAs("Max log size");

				acceptsAll(asList("log-count"), "Specified how many log files to cycle through").withRequiredArg()
						.ofType(Integer.class).defaultsTo(1).describedAs("Log count");

				acceptsAll(asList("log-append"), "Whether to append to the log file").withRequiredArg()
						.ofType(Boolean.class).defaultsTo(true).describedAs("Log append");

				acceptsAll(asList("log-strip-color"), "Strips color codes from log file");

				acceptsAll(asList("c", "config"), "Properties file to use").withRequiredArg().ofType(File.class)
						.defaultsTo(new File("server.properties")).describedAs("Properties file");
			}
		};
		return parser.parse(args);
	}

	private static List<String> asList(String... params) {
		return Arrays.asList(params);
	}
}
