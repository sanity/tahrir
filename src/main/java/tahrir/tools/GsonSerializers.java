package tahrir.tools;

import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;

import com.google.gson.*;

public class GsonSerializers {
	public static class RSAPublicKeySerializer implements JsonSerializer<RSAPublicKey> {
		@Override
		public JsonElement serialize(final RSAPublicKey rsaPublicKey, final Type typeOfId, final JsonSerializationContext context) {
			final JsonObject obj = new JsonObject();
			obj.addProperty("modulus", rsaPublicKey.getModulus());
			obj.addProperty("publicExponent", rsaPublicKey.getPublicExponent());
			return obj;
		}
	}
}
