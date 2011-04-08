package tahrir.io.net;

import java.io.IOException;

import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.net.udp.UdpRemoteAddress;
import tahrir.tools.ByteArraySegment;

public abstract class TrRemoteConnection<RA extends TrRemoteAddress> {

	public abstract void send(final ByteArraySegment message, final double priority,
			final TrSentReceivedListener sentListener) throws IOException;

	public abstract void receive(TrNetworkInterface<RA> iFace, UdpRemoteAddress sender, ByteArraySegment message);
}
