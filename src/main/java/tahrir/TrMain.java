package tahrir;

import java.io.*;

import org.kohsuke.args4j.*;
import org.slf4j.*;

import tahrir.tools.TrUtils;

public class TrMain {

	public static Logger logger = LoggerFactory.getLogger(TrMain.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final CommandLineOptions options = readCommandLineOpts(args);
		final File rootDirectory = new File(options.dir);
		if (!rootDirectory.exists()) {
			logger.info("Creating root directory {}", rootDirectory);
			if (!rootDirectory.mkdir()) {
				logger.error("Failed to create root directory {}", rootDirectory);
				System.exit(-1);
			}
		}
		final TrConfig config = readConfiguration(new File(rootDirectory, options.configFile));
	}

	private static CommandLineOptions readCommandLineOpts(final String[] args) {
		final CommandLineOptions commandLineOptions = new CommandLineOptions();
		final CmdLineParser parser = new CmdLineParser(commandLineOptions);
		final StringBuffer argString = new StringBuffer();
		for (final String a : args) {
			argString.append(a);
			argString.append(' ');
		}
		logger.info("Arguments: " + argString);
		try {
			parser.parseArgument(args);
			if (commandLineOptions.help) {
				// print the list of available options
				parser.printUsage(System.out);
				System.exit(0);
			}
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		return commandLineOptions;
	}

	public static class CommandLineOptions {
		public boolean help = false;
		public String dir = System.getProperty("user.home") + System.getProperty("path.separator") + "tahrir";
		public String configFile = "tahrir.json";
	}

	private static TrConfig readConfiguration(final File file) {
		TrConfig config = new TrConfig();
		if (file.exists()) {
			try {
				config = TrUtils.parseJson(file, TrConfig.class);
			} catch (final Exception e) {
				logger.error("Couldn't read configuration file: " + file, e);
				System.exit(-1);
			}
		} else { // write a new config
			try {
				TrUtils.writeJson(config, file);
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create config file", e);
			}
		}

		return config;
	}
}
