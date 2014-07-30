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

	    //buisness logic is only one line
	    Tuple2<RSAPublicKey, RSAPrivateKey> keyPair= TrCrypto.createRsaKeyPair();

	    //serialize into json
	    IdentityResponse identityResponse=new IdentityResponse();
	    identityResponse.publicKey=keyPair.a;
	    identityResponse.privateKey=keyPair.b;

	    String resp= TrUtils.gson.toJson(identityResponse);

        /*TODO: right now, the private and public key are sent to the GUI in an unencrypted json object.  is this ok?
            i know it's all on the local machine but still seems a bit insecure
         */
	    response.setEntity(resp, MediaType.APPLICATION_JSON);

    }



    private static class IdentityResponse{
        public RSAPublicKey publicKey;
        public RSAPrivateKey privateKey;
    }

}
