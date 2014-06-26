package tahrir.transport.messaging.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.transport.messaging.serialization.TrSerializer;

public class FloatSerializer extends TrSerializer {

	public FloatSerializer() {
		super(Float.class);
	}

	@Override
	protected Float deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readFloat();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeFloat((Float) object);
	}

}
