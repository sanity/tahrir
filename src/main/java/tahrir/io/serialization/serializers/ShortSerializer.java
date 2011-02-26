package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TrSerializer;

public class ShortSerializer extends TrSerializer {

	public ShortSerializer() {
		super(Short.class);
	}

	@Override
	protected Short deserialize(final Type type, final ByteBuffer bb) {
		return new Short(bb.getShort());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putShort((Short) object);
	}

}
