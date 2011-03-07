package tahrir.io.serialization.serializers;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TrSerializer;

public class StringSerializer extends TrSerializer {

	public StringSerializer() {
		super(String.class);
	}

	@Override
	protected String deserialize(final Type type, final ByteBuffer bb) {
		final int length = bb.getInt();
		final byte[] asBytes = new byte[length];
		bb.get(asBytes);
		try {
			return new String(asBytes, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		byte[] asBytes;
		try {
			asBytes = ((String) object).getBytes("UTF-8");

			bb.putInt(asBytes.length);
			bb.put(asBytes);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
