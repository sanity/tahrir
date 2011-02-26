package tahrir.io.serialization.serializers;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import tahrir.io.serialization.TrSerializer;

public class CharSerializer extends TrSerializer {

	public CharSerializer() {
		super(Character.class);
	}

	@Override
	protected Character deserialize(final Type type, final ByteBuffer bb) {
		return new Character(bb.getChar());
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb) {
		bb.putChar((Character) object);
	}

}
