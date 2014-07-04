package tahrir.api;

/**
 * Created by Tejas Dharamsi on 6/25/2014.
 */
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

public class TahrirServer {
    public static void main(String[] args) throws Exception{

        Server server = new Server(Protocol.HTTP,8080,TahrirServerResource.class);
        server.start();

        Component c1 = new Component();
       c1.getServers().add(Protocol.HTTP, 8080);

        GetMessage restlet = new GetMessage();

        c1.getDefaultHost().attach("/fetch_message", restlet);
        c1.start();
    }
}