package tahrir;

import tahrir.io.net.*;

public class TrNode<RA extends TrRemoteAddress> {
	public TrNetworkInterface<RA> networkInterface;

	public TrPeerManager peerManager;

	public TrNode(final TrNetworkInterface<RA> networkInterface) {
		this.networkInterface = networkInterface;
	}
}
