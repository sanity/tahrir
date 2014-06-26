package tahrir.transport.messaging.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.transport.messaging.serialization.TrSerializer;

public class IntegerSerializer extends TrSerializer {

	public IntegerSerializer() {
		super(Integer.class);
	}

	@Override
	protected Integer deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readInt();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeInt((Integer) object);
	}

}
