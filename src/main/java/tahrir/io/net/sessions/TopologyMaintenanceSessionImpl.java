package tahrir.io.net.sessions;

import java.util.*;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

public class TopologyMaintenanceSessionImpl extends TrSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

	private int peersToAccept;

	/**
	 * A responder is the node where the search for a location ends, they will try accept as many
	 * forwarders as possible.
	 */
	private boolean responder;

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

		if (hopsToLive == 0 || closestPeerAddress.equals(node.getRemoteNodeAddress())) {
			// found the closest location or gave up
			responder = true;
			peersToAccept = calcPeersToAccept(requestors);
			sendResponses(requestors);
		}
		else {
			hopsToLive = hopsToLive > TrConstants.MAINTENANCE_HOPS_TO_LIVE
					? TrConstants.HOPS_TO_LIVE_RESET
							: hopsToLive - 1;
			// get next location
			final TopologyMaintenanceSession closestPeerSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(closestPeerAddress.location));
			requestors.add(closestPeerAddress);
			closestPeerSession.probeForLocation(locationToFind, hopsToLive, requestors);
		}
	}

	public void sendResponses(final List<RemoteNodeAddress> requestors) {
		for (int i = 0; i < requestors.size() && peersToAccept > 0; i++) {
			final RemoteNodeAddress requestorAddress = requestors.get(i);
			final TopologyMaintenanceSession requestorSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(requestorAddress.location));

			// tell requestor to accept responder
			requestorSession.acceptMe(node.getRemoteNodeAddress(), node.config.capabilities);
			peersToAccept--;
		}
	}

	public void acceptMe(final RemoteNodeAddress askerAddress, final Capabilities askerCapabilites) {
		// TODO: peers shouldn't just be added like this to end of peers list
		node.peerManager.addNewPeer(askerAddress, askerCapabilites);
		if (!responder) {
			// we have accepted the responder now send them our capabilites so they can accept us
			final TopologyMaintenanceSession requestorSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(askerAddress.location));
			requestorSession.acceptMe(node.getRemoteNodeAddress(), node.config.capabilities);
		}
	}

	private int calcPeersToAccept(final List<RemoteNodeAddress> requestors) {
		return requestors.size() <= node.peerManager.getNumFreePeerSlots()
				? requestors.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}
}
