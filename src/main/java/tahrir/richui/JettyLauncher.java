package tahrir.richui;

import java.net.URL;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyLauncher {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final Server server = new Server(3733);
		final URL resource = JettyLauncher.class.getClassLoader().getResource("tahrir/richui");
		final String warUrlString = resource.toExternalForm();
		server.setHandler(new WebAppContext(warUrlString, "/"));
		server.start();
	}

}
