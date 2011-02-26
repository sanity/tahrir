package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TrSerializer;

public class FloatSerializer extends TrSerializer {

	public FloatSerializer() {
		super(Float.class);
	}

	@Override
	protected Float deserialize(final Type type, final ByteBuffer bb) {
		return new Float(bb.getFloat());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putFloat((Float) object);
	}

}
