package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TahrirSerializer;

public class BooleanSerializer extends TahrirSerializer {

	public BooleanSerializer() {
		super(Boolean.class);
	}

	@Override
	protected Boolean deserialize(final Type type, final ByteBuffer bb) {
		return new Boolean(bb.get() == (byte) 1);
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.put(object.equals(Boolean.TRUE) ? (byte) 1 : (byte) 0);
	}


}
