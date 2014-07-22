package tahrir.api;

import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import java.util.Collection;
import java.util.Hashtable;

/*created by Oliver Lee */


public class RESTAPIMain extends Application {

    private Hashtable messages;
    public static void main(String[]args) throws Exception {

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 18080);

        TahrirRestlet restlet = new TahrirRestlet(component.getDefaultHost());
        component.getDefaultHost().attach("", restlet);

          component.start();

    }
    public RESTAPIMain() {
        super();
        this.messages = new Hashtable();
    }

    @Override
    public Restlet createInboundRoot() {


        Router router = new Router();
        router.attach("/messages", GetMessage.class);
        //router.attach("/messages/{id}",SingleMessage.class);
        router.attach("/identity",GetIdentity.class);
        return router;
    }


    public Message getMessage(String id)
    {
        return (Message)this.messages.get(id);
    }


    public void saveMessage(Message m) {
        this.messages.put(m.getId(), m);
    }


    public void deleteMessage(String id)
    {
        this.messages.remove(id);
    }


    public Collection getMessage() {
        return this.messages.values();
    }

}
