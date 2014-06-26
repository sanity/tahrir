package tahrir.network.sessions;

import java.util.LinkedList;

import tahrir.TrConstants;
import tahrir.network.RemoteNodeAddress;
import tahrir.network.TrPeerManager.Capabilities;
import tahrir.network.TrSession;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> requesters);

	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void sendAcceptInfo(final RemoteNodeAddress acceptor, final LinkedList<RemoteNodeAddress> willConnectTo);

	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void myCapabilitiesAre(final Capabilities myCapabilities, int topologyLocation);
}
