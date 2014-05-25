package tahrir.tools;

import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.io.crypto.TrCrypto;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created with IntelliJ IDEA.
 * User: ian
 * Date: 10/19/13
 * Time: 8:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrUtilsTest {
    @Test
    public void testGsonCustomSerializers() {
        final Tuple2<RSAPublicKey,RSAPrivateKey> rsaKeyPair = TrCrypto.createRsaKeyPair();
        GsonRSAKeys gsonRSAKeys = new GsonRSAKeys();
        gsonRSAKeys.pubKey = rsaKeyPair.a;
        gsonRSAKeys.privKey = rsaKeyPair.b;
        String serialized = TrUtils.gson.toJson(gsonRSAKeys);
        System.out.println("Serialized RSA keys: "+serialized);
        GsonRSAKeys deserializedGsonRSAKeys = TrUtils.gson.fromJson(serialized, GsonRSAKeys.class);
        Assert.assertEquals(deserializedGsonRSAKeys.pubKey, gsonRSAKeys.pubKey);
        Assert.assertEquals(deserializedGsonRSAKeys.privKey, gsonRSAKeys.privKey);
    }

    public static class GsonRSAKeys {
        public RSAPublicKey pubKey;
        public RSAPrivateKey privKey;
    }
}
