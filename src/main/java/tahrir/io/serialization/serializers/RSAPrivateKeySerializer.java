package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import tahrir.io.serialization.TrSerializer;

public class RSAPrivateKeySerializer extends TrSerializer {

	public RSAPrivateKeySerializer() {
		super(RSAPrivateKey.class);
	}

	@Override
	protected RSAPrivateKey deserialize(final Type type, final DataInputStream dis) throws IOException {
		final byte[] bytes = new byte[dis.readInt()];
		dis.read(bytes);
		try {
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		final RSAPrivateKey key = (RSAPrivateKey) object;
		final byte[] encoded = key.getEncoded();
		dos.writeInt(encoded.length);
		dos.write(encoded);
	}


}
