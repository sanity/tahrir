package tahrir.api;//created on 7/18/2014 by QuiteStochastic

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.VirtualHost;
import tahrir.io.crypto.TrCrypto;
import tahrir.tools.GsonSerializers;
import tahrir.tools.Tuple2;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class IdentityRestlet extends org.restlet.Component{

    public IdentityRestlet(VirtualHost host){
        //super();
        //setDefaultHost(host);


        host.attach("/identity/test",new Restlet() {
            @Override
            public void handle(Request request, Response response){



                response.setEntity("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "\n" +
                        "<p>This is /identity/test</p>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>", MediaType.TEXT_HTML);

            }
        });
    }


    @Override
    public void handle(Request request, Response response) {
        /*if you go to the source code of Restlet.java, in the comment above the handle(request, response) method,
            it says:
            "Subclasses overriding this method should make sure that they call
             super.handle(request, response) before adding their own logic."
         */
        super.handle(request, response);


        if(request.getMethod().getName().equals("GET")){

            Tuple2<RSAPublicKey, RSAPrivateKey> keyPair= TrCrypto.createRsaKeyPair();
            JSONObject jsonResponseWithKeyPair=new JSONObject();
            try {

                GsonSerializers.RSAPublicKeySerializer publicKeySerializer=new GsonSerializers.RSAPublicKeySerializer();
                jsonResponseWithKeyPair.append("public_key", publicKeySerializer.serialize(keyPair.a, null, null));

                GsonSerializers.RSAPrivateKeySerializer privateKeySerializer=new GsonSerializers.RSAPrivateKeySerializer();
                jsonResponseWithKeyPair.append("private_key", privateKeySerializer.serialize(keyPair.b, null, null));

            } catch (JSONException e) {
                System.err.println("something wrong with putting keypair in json");
                e.printStackTrace();
            }

            /*TODO: right now, the private and public key are sent to the GUI in an unencrypted json object.  is this ok?
                i know it's all on the local machine but still seems a bit insecure
             */
            response.setEntity(jsonResponseWithKeyPair.toString(), MediaType.APPLICATION_JSON);

        }
        else{
            System.err.println("method not recognized, /identity only uses GET");
        }
    }








}
