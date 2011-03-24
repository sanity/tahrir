package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;

public class ByteSerializer extends TrSerializer {

	public ByteSerializer() {
		super(Byte.class);
	}

	@Override
	protected Byte deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readByte();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeByte((Byte) object);
	}

}
