package tahrir;

import tahrir.io.net.TrRemoteAddress;

/**
 * The root class for the internal state of this node
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 * 
 * @param <RA>
 */
public class TrNode<RA extends TrRemoteAddress> {
	// public TrNetworkInterface<RA> networkInterface;

	public TrPeerManager peerManager;
}
