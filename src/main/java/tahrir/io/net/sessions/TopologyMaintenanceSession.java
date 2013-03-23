package tahrir.io.net.sessions;

import tahrir.TrConstants;
import tahrir.io.net.RemoteNodeAddress;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.io.net.TrSession;

import java.util.Set;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void probeForLocation(final int locationToFind, int hopsToLive, final Set<RemoteNodeAddress> requesters);

	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void sendAcceptInfo(final RemoteNodeAddress acceptor, final Set<RemoteNodeAddress> willConnectTo);

	@Priority(TrConstants.TOPOLOGY_MAINTENANCE_PRIORITY)
	public void myCapabilitiesAre(final Capabilities myCapabilities, int topologyLocation);
}
