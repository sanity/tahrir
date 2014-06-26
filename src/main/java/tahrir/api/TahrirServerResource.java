package tahrir.api;

/**
 * Created by Tejas Dharamsi on 6/25/2014.
 */
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
public class TahrirServerResource extends ServerResource {

    @Get("json")
       public Representation greet(){
        String message="Hello tejas";

        JSONObject jo=new JSONObject();
        try {
            jo.put("message", "True");
        }
        catch(JSONException e)
        {

        }

        Representation rp=new JsonRepresentation(jo);
        return rp;//"Hello Tejas!!! Wassup";
    }
}
