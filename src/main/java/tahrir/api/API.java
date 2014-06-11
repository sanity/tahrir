/**
 * Created by Tejas Dharamsi on 5/8/2014.
 */

import static spark.Spark.*;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayDeque;
import java.util.Deque;

public class API {
    public static Deque<Message> messages = new ArrayDeque<Message>();

    public static void main(String[] args) {

        get(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {
                String title = "Tahrir";
                String createMessageLink = "<a href='/message/create'>New Message</a>";
                StringBuilder html = new StringBuilder();

                html.append("<h1>").append(title).append("</h1>").append("Author: <input type='text' name='user' />").append("<br/>").append(createMessageLink);
                html.append("<hr>");
                /*String User = request.queryParams("user");
                System.out.println(User);*/
                if(API.messages.isEmpty()) {
                    html.append("<b>No articles have been posted</b>");
                } else {
                    for(Message message : API.messages) {
                        String author = request.queryParams("user");
                        html.append(message.getAuthor())
                                .append("&nbsp &nbsp &nbsp &nbsp")
                                .append(message.getCreatedAt())
                                .append("<br/>")
                                .append(message.getContent())
                                .append("<br/>")
                                .append(message.getViewLink())
                                .append("</p>");
                    }
                }

                return html.toString();
            }
        });
        get(new Route("/message/create") {
            @Override
            public Object handle(Request request, Response response) {
                StringBuilder form = new StringBuilder();

                form.append("<form id='message-create-form' method='POST' action='/message/create'>")
                        .append("</form>")
                        .append("<textarea name='message-content' maxlength='140' rows='4' cols='50' form='message-create-form'></textarea>")
                        .append("<br/>")
                        .append("<input type='submit' value='Post' form='message-create-form' />");

                return form.toString();
            }
        });
        post(new Route("/message/create") {
            @Override
            public Object handle(Request request, Response response) {
                String author = "Tejas";
                String content = request.queryParams("message-content");

                Message message = new Message(author,content,API.messages.size() + 1);
                API.messages.addFirst(message);

                response.status(201);
                response.redirect("/");
                return "";
            }
        });
        get(new Route("/article/view/:id") {
            @Override
            public Object handle(Request request, Response response) {
                Integer id = Integer.parseInt(request.params(":id"));
                StringBuilder html = new StringBuilder();

                for(Message message : API.messages) {
                    if(id.equals(message.getId())) {
                        html.append("<a href='/'>Home</a>").append("<p />")
                                .append(message.getAuthor()).append("<br />")
                                .append(message.getCreatedAt())
                                .append("<p>").append(message.getContent()).append("</p>");
                        break;
                    }
                }
                return html.toString();
            }
        });

    }
}