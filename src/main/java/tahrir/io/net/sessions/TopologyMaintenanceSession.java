package tahrir.io.net.sessions;

import java.util.LinkedList;

import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> requesters);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void sendAcceptInfo(final RemoteNodeAddress acceptor, final LinkedList<RemoteNodeAddress> willConnectTo);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void myCapabilitiesAre(final Capabilities myCapabilities);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void myCapabilitiesAre(final Capabilities myCapabilities, final RemoteNodeAddress myAddress);
}
