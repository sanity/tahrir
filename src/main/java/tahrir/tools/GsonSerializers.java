package tahrir.tools;

import com.google.gson.*;
import tahrir.io.crypto.TrCrypto;

import java.lang.reflect.Type;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Custom Gson serializers.
 */
public class GsonSerializers {
	public static class RSAPublicKeySerializer implements JsonSerializer<RSAPublicKey> {
		@Override
		public JsonElement serialize(RSAPublicKey publicKey, Type type,
				JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(TrCrypto.toBase64(publicKey));
		}
	}

	public static class RSAPublicKeyDeserializer implements JsonDeserializer<RSAPublicKey> {
		@Override
		public RSAPublicKey deserialize(JsonElement jsonElement, Type type,
				JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return TrCrypto.decodeBase64PublicKey(jsonElement.getAsJsonPrimitive().getAsString());
		}
	}
    public static class RSAPrivateKeySerializer implements JsonSerializer<RSAPrivateKey> {
   		@Override
   		public JsonElement serialize(RSAPrivateKey privateKey, Type type,
   				JsonSerializationContext jsonSerializationContext) {
   			return new JsonPrimitive(TrCrypto.toBase64(privateKey));
   		}
   	}

   	public static class RSAPrivateKeyDeserializer implements JsonDeserializer<RSAPrivateKey> {
   		@Override
   		public RSAPrivateKey deserialize(JsonElement jsonElement, Type type,
   				JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
   			return TrCrypto.decodeBase64PrivateKey(jsonElement.getAsJsonPrimitive().getAsString());
   		}
   	}

}
