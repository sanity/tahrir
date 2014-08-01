package tahrir.api;

import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.routing.VirtualHost;


/*created by Oliver Lee */

public class TahrirRestlet extends org.restlet.Component {


    public TahrirRestlet(VirtualHost host) {
        /* /////example code
        host.attach("/branch1", new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                if(request.getMethod().getName().equals("GET")){
                    response.setEntity("<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "\n" +
                            "<p>This is branch1</p>\n" +
                            "\n" +
                            "</body>\n" +
                            "</html>", MediaType.TEXT_HTML);
                }
                else if(request.getMethod().getName().equals("POST")){
                    JSONObject j=new JSONObject();
                    try {
                        j.append("key", "val");
                    }
                    catch (org.json.JSONException e){
                        System.err.println("something wrong with json");
                    }

                    response.setEntity(j.toString(), MediaType.APPLICATION_JSON);
                }
                else{
                    System.err.println("method not recognized");
                }
            }
        });

        host.attach("/branch2", new Restlet() {
            @Override
            public void handle(Request request, Response response) {



                response.setEntity("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body>\n" +
                    "\n" +
                    "<p>This is branch2</p>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>", MediaType.TEXT_HTML);
            }
        });*/


        host.attach("/messages", new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                if(request.getMethod().getName().equals("GET")) {

                    response.setEntity("<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "\n" +
                            "<p>you are GETing /messages</p>\n" +
                            "\n" +
                            "</body>\n" +
                            "</html>", MediaType.TEXT_HTML);
                }
                else if(request.getMethod().getName().equals("POST")){

                    response.setEntity("<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "\n" +
                            "<p>you are POSTing /messages</p>\n" +
                            "\n" +
                            "</body>\n" +
                            "</html>", MediaType.TEXT_HTML);

                }
                else{
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    System.err.println("method not recognized, /messages only uses GET and POST");
                }
            }
        });

        host.attach("/messages/boost", new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                if(request.getMethod().getName().equals("GET")) {

                    response.setEntity("<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "\n" +
                            "<p>you are GETing /messages/boost</p>\n" +
                            "\n" +
                            "</body>\n" +
                            "</html>", MediaType.TEXT_HTML);

                }
                else{
                    System.err.println("method not recognized, /boost only uses GET");
                }
            }
        });


        host.attach("/identity", new IdentityRestlet());

    }


    @Override
    public void handle(Request request, Response response) {

        /*if you go to the source code of Restlet.java, in the comment above the handle(request, response) method,
                    it says:
                    "Subclasses overriding this method should make sure that they call
                     super.handle(request, response) before adding their own logic."
                 */
        super.handle(request, response);


        response.setEntity("<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body>\n" +
            "\n" +
            "<p>This is the root</p>\n" +
            "\n" +
            "</body>\n" +
            "</html>", MediaType.TEXT_HTML);
    }

}



