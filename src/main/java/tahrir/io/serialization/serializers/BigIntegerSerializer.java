package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TrSerializer;

public class BigIntegerSerializer extends TrSerializer {

	public BigIntegerSerializer() {
		super(BigInteger.class);
	}

	@Override
	protected BigInteger deserialize(final Type type, final ByteBuffer bb) {
		final int length = bb.getInt();
		final byte[] bytes = new byte[length];
		bb.get(bytes);
		return new BigInteger(bytes);
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.put(((BigInteger) object).toByteArray());
	}


}
