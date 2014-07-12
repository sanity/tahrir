package tahrir.api;

import org.json.JSONObject;

/**
 * Created by Tejas Dharamsi on 7/11/2014.
 */

public class ErrorMessage {

    public JSONObject toJSON() {
        JSONObject jsonobj = new JSONObject();
        try {
            jsonobj.put("error", "An error occured");
            return jsonobj;
        } catch (Exception e) {
            return null;
        }
    }
}
