package tahrir.io.net.sessions;

import java.util.LinkedList;

import tahrir.TrConstants;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> requesters);

	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void sendAcceptInfo(final RemoteNodeAddress acceptor, final LinkedList<RemoteNodeAddress> willConnectTo);

	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void myCapabilitiesAre(final Capabilities myCapabilities, int topologyLocation);
}
