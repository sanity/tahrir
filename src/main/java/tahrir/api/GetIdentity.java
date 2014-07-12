package tahrir.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Created by Tejas Dharamsi on 7/11/2014.
 */
public class GetIdentity extends ServerResource {

    @Get("json")
    public Representation getIdn()
    {
        String publickey="qwertyuiop"; // Here the generated publickey has to be stored
        String privatekey="asdfghjkl"; // Here the generated privatekey has to be stored

        JSONObject key=new JSONObject();
        try
        {
            key.put("public_Key",publickey);
            key.put("private_key",privatekey);

        }
        catch(JSONException e)
        {

        }
        StringRepresentation strrep=new StringRepresentation(key.toString());
        strrep.setMediaType(MediaType.APPLICATION_JSON);

        return strrep;

    }
}
