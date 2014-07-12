package tahrir.api;

/**
 * Created by Tejas Dharamsi on 6/25/2014.
 */

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;


public class TahrirServer extends Application {

    public static void main(String args[])throws Exception
    {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8080);

        TahrirServer restlet = new TahrirServer();
        component.getDefaultHost().attach("", restlet);
        component.start();

    }
    @Override
    public Restlet createInboundRoot() {


        Router router = new Router();
        router.attach("/message", GetMessage.class);
        router.attach("/identity",GetIdentity.class);
        return router;
    }


}