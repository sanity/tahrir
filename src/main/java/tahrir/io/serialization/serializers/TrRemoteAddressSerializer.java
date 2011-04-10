package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;

import tahrir.io.net.TrRemoteAddress;
import tahrir.io.net.udpV1.UdpRemoteAddress;
import tahrir.io.serialization.*;

public class TrRemoteAddressSerializer extends TrSerializer {

	public static final byte UDP_REMOTE_ADDRESS = 0;

	public TrRemoteAddressSerializer() {
		super(TrRemoteAddress.class);
	}

	@Override
	protected TrRemoteAddress deserialize(final Type type, final DataInputStream dis) throws IOException,
	TrSerializableException {
		final byte raType = dis.readByte();
		if (raType == UDP_REMOTE_ADDRESS)
			return TrSerializer.deserializeFrom(UdpRemoteAddress.class, dis);
		else
			throw new TrSerializableException("Unrecognised TrRemoteAddress type: " + raType);
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException,
	TrSerializableException {
		if (!(object instanceof UdpRemoteAddress))
			throw new TrSerializableException("Unrecognized TrRemoteAddress type: " + object.getClass());
		else {
			TrSerializer.serializeTo(object, dos);
		}
	}


}
