package tahrir.api;//created on 7/30/2014 by QuiteStochastic

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;

public class PostMessagesRestlet extends org.restlet.Component{

	@Override
	public void handle(Request request, Response response) {

		response.setEntity("<!DOCTYPE html>\n" +
				"<html>\n" +
				"<body>\n" +
				"\n" +
				"<p>you are POSTing /messages</p>\n" +
				"\n" +
				"</body>\n" +
				"</html>", MediaType.TEXT_HTML);
	}




}
