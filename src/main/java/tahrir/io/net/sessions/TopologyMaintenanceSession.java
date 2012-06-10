package tahrir.io.net.sessions;

import java.util.LinkedList;

import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> requesters);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void accept(final RemoteNodeAddress addressToAccept, final Capabilities capabilitesToAccept, final int peerToAcceptLocation);
}
