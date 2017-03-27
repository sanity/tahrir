package tahrir.api;

import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import java.util.Collection;
import java.util.Hashtable;

/*created by Oliver Lee */


public class RESTAPIMain {


    public static void main(String[]args) throws Exception {

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 18080);

        TahrirRestlet restlet = new TahrirRestlet(component.getDefaultHost());
        component.getDefaultHost().attach("", restlet);

          component.start();

    }

/*
    @Override
    public Restlet createInboundRoot() {


        Router router = new Router();
        router.attach("/messages", GetMessage.class);
        //router.attach("/messages/{id}",SingleMessage.class);
        router.attach("/identity",GetIdentity.class);
        return router;
    }

*/

}
