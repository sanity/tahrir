package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TrSerializer;

public class ByteSerializer extends TrSerializer {

	public ByteSerializer() {
		super(Byte.class);
	}

	@Override
	protected Byte deserialize(final Type type, final ByteBuffer bb) {
		return new Byte(bb.get());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.put((Byte) object);
	}

}
