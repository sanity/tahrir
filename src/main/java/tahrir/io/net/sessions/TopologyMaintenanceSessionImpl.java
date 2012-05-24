package tahrir.io.net.sessions;

import java.util.*;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

public class TopologyMaintenanceSessionImpl extends TrSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

	private boolean isAcceptor;

	public TopologyMaintenanceSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
		logger = LoggerFactory.getLogger(TopologyMaintenanceSessionImpl.class.getName()+" ("+sessionId+")");
	}

	public void startTopologyMaintenance(final int locationToFind, final int hopsToLive) {
		final List<RemoteNodeAddress> requestors = new ArrayList<RemoteNodeAddress>();
		requestors.add(node.getRemoteNodeAddress());
		probeForLocation(locationToFind, hopsToLive, requestors);
	}

	public void probeForLocation(final int locationToFind, int hopsToLive, final List<RemoteNodeAddress> requestors) {
		final RemoteNodeAddress closestPeerAddress = node.peerManager.getClosestPeer(locationToFind);

		if (logger.isDebugEnabled()) {
			logger.debug("The closest peer found was {}", closestPeerAddress);
		}

		if (closestPeerAddress.equals(node.getRemoteNodeAddress())) {
			// found the closest location
			isAcceptor = true;
			sendResponses(requestors);
		} else if (--hopsToLive == 0)
			return;
		else {
			// get next location
			final TopologyMaintenanceSession closestPeerSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(closestPeerAddress.location));
			requestors.add(closestPeerAddress);
			closestPeerSession.probeForLocation(locationToFind, hopsToLive, requestors);
		}
	}

	public void sendResponses(final List<RemoteNodeAddress> requestors) {
		for (int i = 0; i < requestors.size() && canStillAcceptPeers(); i++) {
			final RemoteNodeAddress requestorAddress = requestors.get(i);
			// need to accept requestor and tell them to accept us
			// myCapabilitesAre(node.config.capabilities);
		}
	}

	public void myCapabilitiesAre(final Capabilities myCapabilities) {
		return;
	}

	private boolean canStillAcceptPeers() {
		// TODO: code that checks peer limit is still within threshold so we don't overload acceptor
		return true;
	}
}
