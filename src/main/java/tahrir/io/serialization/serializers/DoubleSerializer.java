package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TahrirSerializer;

public class DoubleSerializer extends TahrirSerializer {

	public DoubleSerializer() {
		super(Double.class);
	}

	@Override
	protected Object deserialize(final Type type, final ByteBuffer bb) {
		return new Double(bb.getDouble());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putDouble((Double) object);
	}

}
