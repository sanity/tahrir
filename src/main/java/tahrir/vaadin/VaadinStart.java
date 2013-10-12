package tahrir.vaadin;

import com.vaadin.server.VaadinServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created with IntelliJ IDEA.
 * User: ian
 * Date: 10/12/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class VaadinStart {
    public static void main(String[] args) throws Exception {
        final Server httpServer = new Server(18080);
        final ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        final VaadinServlet vaadinServlet = new VaadinServlet();
        final ServletHolder vaadinServletHolder = new ServletHolder(vaadinServlet);
        vaadinServletHolder.setInitParameter("UI", "tahrir.vaadin.TestVaadinUI");
        handler.addServlet(vaadinServletHolder, "/*");
        httpServer.setHandler(handler);
        httpServer.start();
        httpServer.join();
    }
}
