package tahrir.api;

/**
 * Created by Tejas Dharamsi on 6/25/2014.
 */
import org.restlet.Server;
import org.restlet.data.Protocol;

public class TahrirServer {
    public static void main(String[] args) throws Exception{

        Server server = new Server(Protocol.HTTP,8080,TahrirServerResource.class);
        server.start();
    }
}