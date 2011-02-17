package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TahrirSerializer;

public class IntegerSerializer extends TahrirSerializer {

	public IntegerSerializer() {
		super(Integer.class);
	}

	@Override
	protected Object deserialize(final Type type, final ByteBuffer bb) {
		return new Integer(bb.getInt());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putInt((Integer) object);
	}

}
