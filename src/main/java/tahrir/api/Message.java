package tahrir.api;

import org.json.JSONObject;

/**
 * Created by Tejas Dharamsi on 7/18/2014.
 */
public class Message {

    private static int idGen = 1;
    private String id = null;
    private String content = null;


    public Message()
    {
        this.id = new String(""+idGen++);
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getContent() {
        return content;
    }


    public void setName(String content) {
        this.content = content;
    }

    /**
     * Convert this object to a JSON object for representation
     */
    public JSONObject toJSON() {
        try{
            JSONObject jsonobj = new JSONObject();
            jsonobj.put("id", this.id);
            jsonobj.put("name", this.content);
            return jsonobj;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Convert this object to a string for representation
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("id:");
        sb.append(this.id);
        sb.append(",Message");
        sb.append(this.content);
        return sb.toString();
    }

    public String toHtml(boolean fragment)
    {
        String retval = "";
        if(fragment) {
            StringBuffer sb = new StringBuffer();
            sb.append("<b>id:</b> ");
            sb.append(id);
            sb.append("<b> Name: </b>");
            sb.append(content);
            sb.append(" <a href=\"/messages/" + id + "\">View</a>");
            sb.append("<br/>");
            retval = sb.toString();
        } else {
            StringBuffer sb = new StringBuffer("<html><head><title>Tahrir</title></head><body><h1>Tahrir Message Dashboard</h1>");
            sb.append("<b>id:</b> ");
            sb.append(id);
            sb.append("<br/><b>Message: </b>");
            sb.append(content);
            sb.append("<br/><br/>Return to <a href=\"/messages\">Messages<a>.</body></html>");
            retval = sb.toString();
        }
        return retval;
    }
}

