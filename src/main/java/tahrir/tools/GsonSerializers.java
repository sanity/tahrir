package tahrir.tools;

import com.google.common.base.Optional;
import com.google.gson.*;
import tahrir.io.crypto.TrCrypto;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
			return TrCrypto.decodeBase64(jsonElement.getAsJsonPrimitive().getAsString());
		}
	}


}
