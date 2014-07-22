package tahrir.api;//created on 7/18/2014 by QuiteStochastic

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import tahrir.io.crypto.TrCrypto;
import tahrir.tools.GsonSerializers;
import tahrir.tools.TrUtils;
import tahrir.tools.Tuple2;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class IdentityRestlet extends org.restlet.Component{

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


            IdentityResponse identityResponse=new IdentityResponse();

            identityResponse.publicKey=keyPair.a;
            identityResponse.privateKey=keyPair.b;

            String resp= TrUtils.gson.toJson(identityResponse);


            /*TODO: right now, the private and public key are sent to the GUI in an unencrypted json object.  is this ok?
                i know it's all on the local machine but still seems a bit insecure
             */
            response.setEntity(resp, MediaType.APPLICATION_JSON);

        }
        else{
            System.err.println("method not recognized, /identity only uses GET");
        }
    }



    public static class IdentityResponse{

        public RSAPublicKey publicKey;


        public RSAPrivateKey privateKey;

    }




}
