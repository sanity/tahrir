package tahrir.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.routing.VirtualHost;


/*created by Oliver Lee */

public class TahrirRestlet extends org.restlet.Component{


    public TahrirRestlet(VirtualHost host) {

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

        /*host.attach("/branch2", new Restlet() {
            @Get("json")
            public Representation greet(){
                String message="Hello tejas";

                JSONObject jo=new JSONObject();
                try {
                    jo.put("message",message);

                }
                catch(JSONException e)
                {

                }

                Representation rp=new JsonRepresentation(jo);

                return rp;
            }
        });*/


    }


    @Override
    public void handle(Request request, Response response) {
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
