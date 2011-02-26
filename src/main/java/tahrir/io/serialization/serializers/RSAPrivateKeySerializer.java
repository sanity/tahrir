package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import tahrir.io.serialization.TrSerializer;

public class RSAPrivateKeySerializer extends TrSerializer {

	public RSAPrivateKeySerializer() {
		super(RSAPrivateKey.class);
	}

	@Override
	protected RSAPrivateKey deserialize(final Type type, final ByteBuffer bb) {
		final byte[] bytes = new byte[bb.getInt()];
		bb.get(bytes);
		try {
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		final RSAPrivateKey key = (RSAPrivateKey) object;
		final byte[] encoded = key.getEncoded();
		bb.putInt(encoded.length);
		bb.put(encoded);
	}


}
