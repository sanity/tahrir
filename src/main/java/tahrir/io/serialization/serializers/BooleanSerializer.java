package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;

public class BooleanSerializer extends TrSerializer {

	public BooleanSerializer() {
		super(Boolean.class);
	}

	@Override
	protected Boolean deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readBoolean();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeBoolean((Boolean) object);
	}


}
