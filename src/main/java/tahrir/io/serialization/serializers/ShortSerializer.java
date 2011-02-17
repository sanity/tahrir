package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TahrirSerializer;

public class ShortSerializer extends TahrirSerializer {

	public ShortSerializer() {
		super(Short.class);
	}

	@Override
	protected Object deserialize(final Type type, final ByteBuffer bb) {
		return new Short(bb.getShort());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putShort((Short) object);
	}

}
