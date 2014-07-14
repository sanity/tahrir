package tahrir.api;

import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/*created by Oliver Lee */


public class RESTAPIMain {


    public static void main(String[]args) throws Exception {

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 18080);

        TahrirRestlet restlet = new TahrirRestlet(component.getDefaultHost());
        component.getDefaultHost().attach("", restlet);

        /*component.getDefaultHost().attach("/branch1", new Restlet() {
            @Override
            public void handle(Request request, Response response){

                response.setEntity("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "\n" +
                        "<p>This is branch1</p>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>", MediaType.TEXT_HTML);
            }
        });*/


        component.start();

    }

/*
    @Override
    public Restlet createInboundRoot() {


        Router router = new Router();
        router.attach("/message", GetMessage.class);
        router.attach("/identity",GetIdentity.class);
        return router;
    }*/
}
