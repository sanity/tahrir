package tahrir.io.net.sessions;

import java.util.List;

import tahrir.io.net.*;

public interface TopologyMaintenanceSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void probeForLocation(final int locationToFind, int hopsToLive, final List<RemoteNodeAddress> requestors);
}
