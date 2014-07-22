package tahrir.api;



/**
 * Created by Tejas Dharamsi on 7/4/2014.
 */


import java.util.Collection;
import org.json.JSONArray;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.*;

import static org.restlet.engine.converter.ConverterUtils.getVariants;


public class GetMessage extends ServerResource {

      private Collection messages = null;
    RESTAPIMain app;

    public GetMessage(Context context, Request request, Response response) {
        super();


        this.app = (RESTAPIMain) this.getApplication();
        this.messages = app.getMessage();


        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }


    public boolean allowPost() {
        return true;
    }


    public boolean setReadable() {
        return true;
    }


    public Representation represent(Variant variant) throws ResourceException {
        Representation result = null;
        if (null == this.messages) {
            ErrorMessage em = new ErrorMessage();
            return representError(variant, em);
        } else {

            if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {

                JSONArray MessageArray = new JSONArray();
                for(Object o : this.messages) {
                    Message m = (Message)o;
                    MessageArray.put(m.toJSON());
                }

                result = new JsonRepresentation(MessageArray);

            } else {

                // create a plain text representation of our list of widgets
                StringBuffer buf = new StringBuffer("<html><head><title>Tahrir</title><head><body><h1>Tahrir Message Dashboard</h1>");
                buf.append("<form name=\"input\" action=\"/messages\" method=\"POST\">");
                buf.append("Message ");
                buf.append("<input type=\"text\" name=\"name\" />");
                buf.append("<input type=\"submit\" value=\"Submit\" />");
                buf.append("</form>");
                buf.append("<br/><h2> There are " + this.messages.size() + " total.</h2>");
                for(Object o : this.messages) {
                    Message m = (Message)o;
                    buf.append(m.toHtml(true));
                }
                buf.append("</body></html>");
                result = new StringRepresentation(buf.toString());
                result.setMediaType(MediaType.TEXT_HTML);
            }
        }
        return result;
    }


    public void acceptRepresentation(Representation entity)
            throws ResourceException {

        try {
            if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
                    true))
            {
                // Use the incoming data in the POST request to create/store a new widget resource.
                Form form = new Form(entity);
                Message m = new Message();
                m.setName(form.getFirstValue("name"));
                this.app.saveMessage(m);

                getResponse().setStatus(Status.SUCCESS_OK);
                //Representation rep = new JsonRepresentation(w.toJSON());
                Representation rep = new StringRepresentation(m.toHtml(false));
                rep.setMediaType(MediaType.TEXT_HTML);
                getResponse().setEntity(rep);
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }


    private Representation representError(Variant variant, ErrorMessage em)
            throws ResourceException {
        Representation result = null;
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            result = new JsonRepresentation(em.toJSON());
        } else {
            result = new StringRepresentation(em.toString());
        }
        return result;
    }

    protected Representation representError(MediaType type, ErrorMessage em)
            throws ResourceException {
        Representation result = null;
        if (type.equals(MediaType.APPLICATION_JSON)) {
            result = new JsonRepresentation(em.toJSON());
        } else {
            result = new StringRepresentation(em.toString());
        }
        return result;
    }
}