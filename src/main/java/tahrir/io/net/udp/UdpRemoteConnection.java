package tahrir.io.net.udp;

import java.io.IOException;

import org.slf4j.*;

import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.tools.ByteArraySegment;

public abstract class UdpRemoteConnection extends TrRemoteConnection<UdpRemoteAddress> {
	final Logger logger = LoggerFactory.getLogger(UdpRemoteConnection.class);

	public void received(final TrNetworkInterface<UdpRemoteAddress> iFace, final UdpRemoteAddress sender,
			final ByteArraySegment message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(final ByteArraySegment message, final double priority, final TrSentReceivedListener sentListener)
	throws IOException {
		// TODO Auto-generated method stub

	}

}
