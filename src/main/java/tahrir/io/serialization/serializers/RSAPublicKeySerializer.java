package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import tahrir.io.serialization.TrSerializer;
import tahrir.tools.TrUtils;

public class RSAPublicKeySerializer extends TrSerializer {

	public RSAPublicKeySerializer() {
		super(RSAPublicKey.class);
	}

	@Override
	public RSAPublicKey deserialize(final Type type, final DataInputStream dis) throws IOException {
		final byte[] bytes = new byte[dis.readInt()];
		TrUtils.readAllBytes(bytes, dis);
		try {
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		final RSAPublicKey key = (RSAPublicKey) object;
		final byte[] encoded = key.getEncoded();
		dos.writeInt(encoded.length);
		dos.write(encoded);
	}


}
