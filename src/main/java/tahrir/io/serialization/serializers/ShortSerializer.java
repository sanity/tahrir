package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;

public class ShortSerializer extends TrSerializer {

	public ShortSerializer() {
		super(Short.class);
	}

	@Override
	protected Short deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readShort();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeShort((Short) object);
	}

}
