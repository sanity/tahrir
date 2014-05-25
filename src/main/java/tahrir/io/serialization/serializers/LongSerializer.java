package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;

public class LongSerializer extends TrSerializer {

	public LongSerializer() {
		super(Long.class);
	}

	@Override
	protected Long deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readLong();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeLong((Long) object);
	}

}
