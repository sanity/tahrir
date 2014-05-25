package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;

import tahrir.io.serialization.*;

public class InetAddressSerializer extends TrSerializer {

	public static final byte UDP_REMOTE_ADDRESS = 0;

	public InetAddressSerializer() {
		super(InetAddress.class);
	}

	@Override
	protected InetAddress deserialize(final Type type, final DataInputStream dis) throws IOException,
	TrSerializableException {
		final byte[] addr = new byte[dis.readInt()];
		dis.read(addr);
		return InetAddress.getByAddress(addr);
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException,
	TrSerializableException {
		final InetAddress addr = (InetAddress) object;
		final byte[] ba = addr.getAddress();
		dos.writeInt(ba.length);
		dos.write(ba);
	}


}
