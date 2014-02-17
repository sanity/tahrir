package tahrir;

import com.vaadin.server.VaadinServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.tools.TrUtils;
import tahrir.ui.TrMainWindow;
import tahrir.vaadin.TahrirVaadinRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

public class TrMain {

	public static Logger logger = LoggerFactory.getLogger(TrMain.class);

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
		final TrMainConfig config = readConfiguration(new File(rootDirectory, options.configFile));

        try {
            final TrNode node = TrUtils.TestUtils.makeNode(9003, false, false, false, true, 0, 0);
            if(config.startGui){ //this is the vaadin branch, so this will be false
                final TrMainWindow mainWindow = new TrMainWindow(node, "Default");
                mainWindow.getContentPanel().revalidate();
            }
            else{
                System.out.println("will now make vaadin server");
                final Server httpServer = new Server(18080);
                final ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
                handler.setContextPath("/");

                final VaadinServlet vaadinServlet = new VaadinServlet(){

                    protected TahrirVaadinRequest createVaadinRequest(
                            HttpServletRequest request) {
                        return new TahrirVaadinRequest(request, getService(), node);
                    }

                };
                final ServletHolder vaadinServletHolder = new ServletHolder(vaadinServlet);
                vaadinServletHolder.setInitParameter("UI", "tahrir.vaadin.TestVaadinUI");

                handler.addServlet(vaadinServletHolder, "/*");
                httpServer.setHandler(handler);
                httpServer.start();
                httpServer.join();

                System.out.println("done making vaadin server");

            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("main method complete");

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
		public String dir = System.getProperty("user.home") + "/tahrir";
		public String configFile = "tahrir.json";
	}

	private static TrMainConfig readConfiguration(final File file) {
        TrMainConfig config = new TrMainConfig();
		if (file.exists()) {
			try {
				config = TrUtils.parseJson(file, TrMainConfig.class);
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

    public static class TrMainConfig {
        public boolean startGui = false;

        public TrNodeConfig node = new TrNodeConfig();
    }
}
