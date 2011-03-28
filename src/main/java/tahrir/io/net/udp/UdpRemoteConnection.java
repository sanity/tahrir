package tahrir.io.net.udp;

import tahrir.io.net.TrRemoteConnection;

public class UdpRemoteConnection extends TrRemoteConnection {
	private final UdpNetworkInterface iface;

	protected UdpRemoteConnection(final UdpNetworkInterface iface, final UdpRemoteAddress address) {
		this.iface = iface;

	}
}
