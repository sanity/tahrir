package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.serialization.TrSerializer;
import tahrir.tools.*;

public class ByteArraySegmentSerializer extends TrSerializer {

	public ByteArraySegmentSerializer() {
		super(ByteArraySegment.class);
	}

	@Override
	protected ByteArraySegment deserialize(final Type type, final DataInputStream dis) throws IOException {
		final int length = dis.readInt();
		final byte[] data = new byte[length];
		TrUtils.readAllBytes(data, dis);
		return new ByteArraySegment(data);
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException {
		final ByteArraySegment bas = (ByteArraySegment) object;
		dos.writeInt(bas.length);
		bas.writeTo(dos);
	}

}
