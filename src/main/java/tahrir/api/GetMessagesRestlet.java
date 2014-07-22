package tahrir.api;
// created on 7/22/2014 by QuiteStochastic

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import tahrir.tools.TrUtils;

import java.util.ArrayList;

public class GetMessagesRestlet extends org.restlet.Component{




    @Override
    public void handle(Request request, Response response) {

        //as of july 2014 when this code is written, business logic does not exist yet,
        // however when it does, it will somehow populate this array list
        ArrayList<GetMessagesResponse.GetMessagesResponseMessage> messagesList=
                new ArrayList<GetMessagesResponse.GetMessagesResponseMessage>();

        //since business logic does not exist yet, the following is some stand in filler
        messagesList.add(new GetMessagesResponse.GetMessagesResponseMessage(
                "pubkeyjdfjldflkdskj", "sanity",1234,"hashoerijdfjgpsdfogsdf","hello"));
        messagesList.add(new GetMessagesResponse.GetMessagesResponseMessage(
                "pubkeyaadfsdjndasjfhasdlf", "insanity",4321,"hashqrewrwerwewesdafaf","hello again"));


        //serialize into json
        GetMessagesResponse getMessagesResponse=new GetMessagesResponse();
        getMessagesResponse.messageses=messagesList;

        String resp=TrUtils.gson.toJson(getMessagesResponse);

        response.setEntity(resp,MediaType.APPLICATION_JSON);

        /*response.setEntity("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<p>you are GETing /messages</p>\n" +
                "\n" +
                "</body>\n" +
                "</html>", MediaType.TEXT_HTML);*/

    }



    private static class GetMessagesResponse{

        /*
        {
            "messages":[
                {
                    "author_pubkey":"...",
                    "author_nick":"sanity",
                    "message_id":124,
                    "message_hash":"4z...E8",
                    "content":"hi"
                },
                {
                    "author_pubkey":"...",
                    "author_nick":"some_other_Guy",
                    "message_id":534,
                    "message_hash":"dsfgdfsgsdf",
                    "content":"teeheehe"
                }
            ]
        }*/

        public ArrayList<GetMessagesResponseMessage> messageses=new ArrayList<GetMessagesResponseMessage>();


        private static class GetMessagesResponseMessage{

            public GetMessagesResponseMessage(String author_pubkey, String author_nick, int message_id,
                                              String message_hash, String content){

                this.author_pubkey=author_pubkey;
                this.author_nick=author_nick;
                this.message_id=message_id;
                this.message_hash=message_hash;
                this.content=content;
            }

            public String author_pubkey;
            public String author_nick;
            public int message_id;
            public String message_hash;
            public String content;
        }


    }
}
