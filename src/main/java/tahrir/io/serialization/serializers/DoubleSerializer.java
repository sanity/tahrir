package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;

public class DoubleSerializer extends TrSerializer {

	public DoubleSerializer() {
		super(Double.class);
	}

	@Override
	protected Double deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readDouble();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeDouble((Double) object);
	}

}
