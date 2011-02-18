package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TahrirSerializer;

public class StringSerializer extends TahrirSerializer {

	public StringSerializer() {
		super(String.class);
	}

	@Override
	protected String deserialize(final Type type, final ByteBuffer bb) {
		final int length = bb.getInt();
		final byte[] asBytes = new byte[length];
		bb.get(asBytes);
		return new String(asBytes);
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		final byte[] asBytes = ((String) object).getBytes();
		bb.putInt(asBytes.length);
		bb.put(asBytes);
	}

}
