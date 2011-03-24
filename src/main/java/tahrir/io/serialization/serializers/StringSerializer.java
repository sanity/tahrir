package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;

public class StringSerializer extends TrSerializer {

	public StringSerializer() {
		super(String.class);
	}

	@Override
	protected String deserialize(final Type type, final DataInputStream dis) throws IOException {
		return dis.readUTF();
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		dos.writeUTF((String) object);
	}

}
