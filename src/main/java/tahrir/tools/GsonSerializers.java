package tahrir.tools;

import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;

import com.google.gson.*;

public class GsonSerializers {
	public static class RSAPublicKeySerializer implements JsonSerializer<RSAPublicKey> {
		@Override
		public JsonElement serialize(final RSAPublicKey rsaPublicKey, final Type typeOfId, final JsonSerializationContext context) {
			final JsonArray bytesArray = new JsonArray();
			for (final byte b : rsaPublicKey.getEncoded()) {
				bytesArray.add(new JsonPrimitive(b));
			}
			return bytesArray;
		}
	}

	public static class RSAPublicKeyDeserializer implements JsonDeserializer<RSAPublicKey> {
		@Override
		public RSAPublicKey deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
				throws JsonParseException {
			final JsonArray jsonArray = json.getAsJsonArray();
			final byte[] bytes = new byte[jsonArray.size()];
			for (int i = 0; i < jsonArray.size(); i++) {
				bytes[i] = jsonArray.get(i).getAsByte();
			}
			return TrUtils.getPublicKey(bytes);
		}
	}
}
