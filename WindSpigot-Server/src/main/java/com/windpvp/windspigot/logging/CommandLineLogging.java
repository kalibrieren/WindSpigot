package com.windpvp.windspigot.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.RegexReplacement;
import org.bukkit.craftbukkit.LoggerOutputStream;
import org.bukkit.craftbukkit.util.ForwardLogHandler;

import jline.console.ConsoleReader;
import joptsimple.OptionException;
import joptsimple.OptionSet;

/**
 * Applies CraftBukkit {@code Main} command-line logging options
 * ({@code --log-pattern}, {@code --log-limit}, {@code --log-count},
 * {@code --log-append}, {@code --log-strip-color}, {@code --date-format}) to
 * the Log4j file appender. Defaults remain {@code log4j2.xml} until an option
 * is present on the command line.
 * <p>
 * Uses the Log4j 2.0-era factory APIs (compatible with the copy shaded into
 * {@code minecraft-server}) plus a JUL {@link FileHandler} for rotation
 * semantics that match the historical CraftBukkit CLI flags.
 */
public final class CommandLineLogging {

	static final String DEFAULT_FILE_NAME = "logs/latest.log";
	static final String DEFAULT_FILE_PATTERN = "logs/%d{yyyy-MM-dd}-%i.log.gz";
	static final String DEFAULT_DATE_PATTERN = "HH:mm:ss";
	static final String FILE_APPENDER_NAME = "File";

	private CommandLineLogging() {
	}

	/**
	 * Resolved view of CraftBukkit logging CLI flags (testable without Log4j).
	 */
	public static final class Settings {
		public final String fileName;
		public final String rolloverPattern;
		public final int limitBytes;
		public final int count;
		public final boolean append;
		public final boolean stripColor;
		public final String datePattern;
		public final boolean reconfigureFileAppender;

		Settings(String fileName, String rolloverPattern, int limitBytes, int count, boolean append,
				boolean stripColor, String datePattern, boolean reconfigureFileAppender) {
			this.fileName = fileName;
			this.rolloverPattern = rolloverPattern;
			this.limitBytes = limitBytes;
			this.count = count;
			this.append = append;
			this.stripColor = stripColor;
			this.datePattern = datePattern;
			this.reconfigureFileAppender = reconfigureFileAppender;
		}

		/** Log4j conversion pattern for the file appender (no {@code %replace}). */
		public String fileLayoutPattern() {
			return "[%d{" + datePattern + "}] [%t/%level]: %msg%n";
		}

		/**
		 * JUL {@link FileHandler} pattern. When {@code count > 1} and the name has no
		 * {@code %g}, append {@code .%g} so rotation can distinguish generations.
		 */
		public String julFilePattern() {
			if (count > 1 && fileName.indexOf("%g") < 0) {
				return fileName + ".%g";
			}
			return fileName;
		}
	}

	/**
	 * Parse {@link OptionSet} values declared in {@code org.bukkit.craftbukkit.Main}.
	 */
	public static Settings resolve(OptionSet options) {
		if (options == null) {
			return defaultsWithoutReconfigure();
		}

		boolean hasPattern = options.has("log-pattern");
		boolean hasLimit = options.has("log-limit");
		boolean hasCount = options.has("log-count");
		boolean hasAppend = options.has("log-append");
		boolean hasStrip = options.has("log-strip-color");
		boolean hasDate = options.has("date-format");
		boolean reconfigure = hasPattern || hasLimit || hasCount || hasAppend || hasStrip || hasDate;

		String fileName = hasPattern ? stringValue(options, "log-pattern", DEFAULT_FILE_NAME) : DEFAULT_FILE_NAME;
		int limitBytes = intValue(options, "log-limit", 0);
		int count = Math.max(1, intValue(options, "log-count", 1));
		boolean append = booleanValue(options, "log-append", true);
		String datePattern = resolveDatePattern(options, hasDate);

		String rolloverPattern = hasPattern ? fileName + ".%i" : DEFAULT_FILE_PATTERN;

		return new Settings(fileName, rolloverPattern, limitBytes, count, append, hasStrip, datePattern, reconfigure);
	}

	private static Settings defaultsWithoutReconfigure() {
		return new Settings(DEFAULT_FILE_NAME, DEFAULT_FILE_PATTERN, 0, 1, true, false, DEFAULT_DATE_PATTERN, false);
	}

	private static String resolveDatePattern(OptionSet options, boolean hasDate) {
		if (!hasDate) {
			return DEFAULT_DATE_PATTERN;
		}
		try {
			Object object = options.valueOf("date-format");
			if (object instanceof SimpleDateFormat) {
				String pattern = ((SimpleDateFormat) object).toPattern();
				if (pattern != null && !pattern.isEmpty()) {
					return pattern;
				}
			}
		} catch (OptionException ex) {
			System.err.println("Given date format is not valid. Falling back to default.");
		}
		return DEFAULT_DATE_PATTERN;
	}

	private static String stringValue(OptionSet options, String name, String fallback) {
		try {
			Object value = options.valueOf(name);
			if (value instanceof String && !((String) value).isEmpty()) {
				return (String) value;
			}
		} catch (OptionException ignored) {
			// fall through
		}
		return fallback;
	}

	private static int intValue(OptionSet options, String name, int fallback) {
		try {
			Object value = options.valueOf(name);
			if (value instanceof Integer) {
				return ((Integer) value).intValue();
			}
		} catch (OptionException ignored) {
			// fall through
		}
		return fallback;
	}

	private static boolean booleanValue(OptionSet options, String name, boolean fallback) {
		try {
			Object value = options.valueOf(name);
			if (value instanceof Boolean) {
				return ((Boolean) value).booleanValue();
			}
		} catch (OptionException ignored) {
			// fall through
		}
		return fallback;
	}

	/**
	 * JUL forwarder + optional Log4j file-appender reconfiguration + console redirect.
	 */
	public static void setup(OptionSet options, ConsoleReader reader) {
		Settings settings = resolve(options);

		java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
		global.setUseParentHandlers(false);
		for (java.util.logging.Handler handler : global.getHandlers()) {
			global.removeHandler(handler);
		}
		global.addHandler(new ForwardLogHandler());

		final Logger logger = (Logger) LogManager.getRootLogger();
		for (org.apache.logging.log4j.core.Appender appender : logger.getAppenders().values()) {
			if (appender instanceof org.apache.logging.log4j.core.appender.ConsoleAppender) {
				logger.removeAppender(appender);
			}
		}

		if (settings.reconfigureFileAppender) {
			try {
				applyFileAppender(settings);
			} catch (IOException ex) {
				System.err.println("Failed to apply command-line logging arguments: " + ex.getMessage());
				ex.printStackTrace(System.err);
			}
		}

		new Thread(new org.bukkit.craftbukkit.util.TerminalConsoleWriterThread(System.out, reader)).start();

		System.setOut(new PrintStream(new LoggerOutputStream(logger, Level.INFO), true));
		System.setErr(new PrintStream(new LoggerOutputStream(logger, Level.WARN), true));
	}

	/**
	 * Replace the root {@code File} appender to honor CLI logging options.
	 */
	static void applyFileAppender(Settings settings) throws IOException {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig root = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

		org.apache.logging.log4j.core.Appender previous = root.getAppenders().get(FILE_APPENDER_NAME);
		if (previous != null) {
			root.removeAppender(FILE_APPENDER_NAME);
			previous.stop();
		}

		RegexReplacement strip = settings.stripColor
				? RegexReplacement.createRegexReplacement("\u00A7.", "")
				: null;
		PatternLayout layout = PatternLayout.createLayout(settings.fileLayoutPattern(), config, strip, null, null);

		CliFileAppender replacement = new CliFileAppender(FILE_APPENDER_NAME, layout, settings);
		replacement.start();
		root.addAppender(replacement, null, null);
		ctx.updateLoggers();
	}

	/**
	 * Log4j appender backed by JUL {@link FileHandler} so {@code --log-pattern}/
	 * {@code --log-limit}/{@code --log-count}/{@code --log-append} match historical
	 * CraftBukkit semantics.
	 */
	static final class CliFileAppender extends AbstractAppender {
		private final FileHandler fileHandler;

		CliFileAppender(String name, PatternLayout layout, Settings settings) throws IOException {
			super(name, null, layout, false);
			this.fileHandler = new FileHandler(settings.julFilePattern(), settings.limitBytes, settings.count,
					settings.append);
			this.fileHandler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage();
				}
			});
		}

		@Override
		public void append(LogEvent event) {
			byte[] bytes = getLayout().toByteArray(event);
			String message;
			try {
				message = new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				message = new String(bytes);
			}
			LogRecord record = new LogRecord(java.util.logging.Level.INFO, message);
			fileHandler.publish(record);
		}

		@Override
		public void stop() {
			fileHandler.close();
			super.stop();
		}
	}
}
