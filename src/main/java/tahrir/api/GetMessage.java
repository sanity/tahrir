package tahrir.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.internal.matchers.Null;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import java.util.Date;

/**
 * Created by Tejas Dharamsi on 7/4/2014.
 */


public class GetMessage extends ServerResource {

    @Get("json")
    public JSONObject getMsg(String author,Date before,Date after){
        String message="Hello tejas";

        JSONObject jo=new JSONObject();
        try {
            jo.put("message", message);
            jo.put("hey", "tejas");
        }
        catch(JSONException e)
        {

        }
        /*StringRepresentation stringRep = new StringRepresentation(jo.toString());
        stringRep.setMediaType(MediaType.APPLICATION_JSON);*/

        return jo;
    }
     static int message_id;
    @Post("json")
    public Representation postMsg()
    {
        String author="soso";   // method to fetch author;
        String content="gagcsvfjnwlg"; // method to have content;
        message_id=1000;
        String privatekey="zxcvbnm";
       JSONObject jo=new JSONObject();
        ErrorMessage err=new ErrorMessage();
        StringRepresentation stringRep;
        if(author!= null) {
            try {
                jo.put("Status", "Okay");
                jo.put("message_id", message_id);
                jo.put("hash", "xyz");
                message_id++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            stringRep = new StringRepresentation(jo.toString());
            stringRep.setMediaType(MediaType.APPLICATION_JSON);
        }
        else
        {
             stringRep = new StringRepresentation(err.toString());
            stringRep.setMediaType(MediaType.APPLICATION_JSON);
         }
        return stringRep;



    }
}
