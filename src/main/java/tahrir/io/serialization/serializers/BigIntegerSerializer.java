package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;

import tahrir.io.serialization.TrSerializer;
import tahrir.tools.TrUtils;

public class BigIntegerSerializer extends TrSerializer {

	public BigIntegerSerializer() {
		super(BigInteger.class);
	}

	@Override
	protected BigInteger deserialize(final Type type, final DataInputStream dis) throws IOException {
		final int length = dis.readInt();
		final byte[] bytes = new byte[length];
		TrUtils.readAllBytes(bytes, dis);
		return new BigInteger(bytes);
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.write(((BigInteger) object).toByteArray());
	}


}
