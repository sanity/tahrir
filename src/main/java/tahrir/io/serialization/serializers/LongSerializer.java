package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TahrirSerializer;

public class LongSerializer extends TahrirSerializer {

	public LongSerializer() {
		super(Long.class);
	}

	@Override
	protected Object deserialize(final Type type, final ByteBuffer bb) {
		return new Long(bb.getLong());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putLong((Long) object);
	}

}
