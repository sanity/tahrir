package tahrir.transport.messaging.serialization.serializers;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;

import tahrir.network.PhysicalNetworkLocation;
import tahrir.transport.messaging.udpV1.UdpNetworkLocation;
import tahrir.transport.messaging.serialization.*;

public class NetworkLocationSerializer extends TrSerializer {

	public static final byte UDP_REMOTE_ADDRESS = 0;

	public NetworkLocationSerializer() {
		super(PhysicalNetworkLocation.class);
	}

	@Override
	protected PhysicalNetworkLocation deserialize(final Type type, final DataInputStream dis) throws IOException,
	TrSerializableException {
		final byte raType = dis.readByte();
		if (raType == UDP_REMOTE_ADDRESS)
			return new UdpNetworkLocation(TrSerializer.deserializeFrom(InetAddress.class, dis), dis.readInt());
		else
			throw new TrSerializableException("Unrecognised TrRemoteAddress type: " + raType);
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos) throws IOException,
	TrSerializableException {
		if (!(object instanceof UdpNetworkLocation))
			throw new TrSerializableException("Unrecognized TrRemoteAddress type: " + object.getClass());
		else {
			dos.writeByte(UDP_REMOTE_ADDRESS);
			final UdpNetworkLocation ura = (UdpNetworkLocation) object;
			TrSerializer.serializeTo(ura.inetAddress, dos);
			dos.writeInt(ura.port);
		}
	}


}
