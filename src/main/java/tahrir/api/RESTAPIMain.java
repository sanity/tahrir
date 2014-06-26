package tahrir.api;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class RESTAPIMain {


    public static void main(String[]args) throws Exception {

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 18080);

        TahrirRestlet restlet = new TahrirRestlet();

        component.getDefaultHost().attach("", restlet);
        component.start();

    }



}
