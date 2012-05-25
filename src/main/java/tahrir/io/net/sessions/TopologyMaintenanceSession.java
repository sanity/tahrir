package tahrir.io.net.sessions;

import java.util.List;

import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void probeForLocation(final int locationToFind, int hopsToLive, final List<RemoteNodeAddress> requestors);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void acceptMe(RemoteNodeAddress askerAddress, Capabilities askerCapabilites);
}
