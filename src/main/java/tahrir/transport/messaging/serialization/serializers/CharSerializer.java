package tahrir.transport.messaging.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.transport.messaging.serialization.TrSerializer;

public class CharSerializer extends TrSerializer {

	public CharSerializer() {
		super(Character.class);
	}

	@Override
	protected Character deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readChar();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeChar((Character) object);
	}

}
