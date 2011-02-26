package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import tahrir.io.serialization.TrSerializer;

public class RSAPublicKeySerializer extends TrSerializer {

	public RSAPublicKeySerializer() {
		super(RSAPublicKey.class);
	}

	@Override
	protected RSAPublicKey deserialize(final Type type, final ByteBuffer bb) {
		final byte[] bytes = new byte[bb.getInt()];
		bb.get(bytes);
		try {
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		final RSAPublicKey key = (RSAPublicKey) object;
		final byte[] encoded = key.getEncoded();
		bb.putInt(encoded.length);
		bb.put(encoded);
	}


}
