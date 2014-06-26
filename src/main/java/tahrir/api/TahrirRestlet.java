package tahrir.api;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

public class TahrirRestlet extends Restlet{

    @Override
    public void handle(Request request, Response response) {
    response.setEntity("<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body>\n" +
            "\n" +
            "<p>Hello World</p>\n" +
            "\n" +
            "</body>\n" +
            "</html>", MediaType.TEXT_HTML);
    }
}
