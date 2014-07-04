package tahrir.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Restlet;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Created by Tejas Dharamsi on 6/27/2014.
 */

public class GetMessage extends Restlet {

    @Get("json")
    public Representation greet(){
        String message="Hello tejas";

        JSONObject jo=new JSONObject();
        try {
            jo.put("message","message");

        }
       catch(JSONException e)
        {

        }

        Representation rp=new JsonRepresentation(jo);

        return rp;//"Hello Tejas!!! Wassup";
    }
}
