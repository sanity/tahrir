package tahrir.io.net.sessions;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.tools.TrUtils;

public class TopologyMaintenanceSessionImpl extends TrSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

	private static boolean fowardedRecently = false;

	private int peersToAccept;

	/**
	 * The requester is the node where the search initiates.
	 */
	private boolean requester = false;


	/**
	 * A responder is the node where the search for a location ends, they will try accept as many
	 * forwarders as possible.
	 */
	private boolean responder = false;

	public TopologyMaintenanceSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
		logger = LoggerFactory.getLogger(TopologyMaintenanceSessionImpl.class.getName()+" ("+sessionId+")");
		TrUtils.executor.scheduleWithFixedDelay(new FowardedRecentlyReset(), 0, TrConstants.FORWARD_AGAIN_WAIT_SEC, TimeUnit.SECONDS);
	}

	public void startTopologyMaintenance(final int locationToFind, final int hopsToLive) {
		logger.debug("Starting maintenance from {} with location {}", node.getRemoteNodeAddress(), locationToFind);
		requester = true;
		final List<RemoteNodeAddress> requestors = new ArrayList<RemoteNodeAddress>();
		requestors.add(node.getRemoteNodeAddress());
		probeForLocation(locationToFind, hopsToLive, requestors);
	}

	public void probeForLocation(final int locationToFind, int hopsToLive, final List<RemoteNodeAddress> requesters) {
		if (requester || !fowardedRecently) {
			fowardedRecently = true;

			final RemoteNodeAddress closestPeerAddress = node.peerManager.getClosestPeer(locationToFind);

			if (logger.isDebugEnabled()) {
				logger.debug("The closest peer found was {}", closestPeerAddress);
			}

			if (hopsToLive == 0 || closestPeerAddress.equals(node.getRemoteNodeAddress())) {
				// the current node is the closest to what we're looking for or we've given up
				responder = true;
				peersToAccept = calcPeersToAccept(requesters);
				sendResponses(requesters);
			} else {
				hopsToLive = hopsToLive > TrConstants.MAINTENANCE_HOPS_TO_LIVE
						? TrConstants.HOPS_TO_LIVE_RESET
								: hopsToLive - 1;
				node.peerManager.updateTimeLastUsed(closestPeerAddress.location);
				// get next location
				final TopologyMaintenanceSession closestPeerSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(closestPeerAddress.location));
				requesters.add(closestPeerAddress);
				closestPeerSession.probeForLocation(locationToFind, hopsToLive, requesters);
			}
		}
	}

	public void sendResponses(final List<RemoteNodeAddress> requesters) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} is sending reponses to {}", node.getRemoteNodeAddress(), requesters);
		}

		for (int i = 0; i < requesters.size() && peersToAccept > 0; i++) {
			final RemoteNodeAddress requestorAddress = requesters.get(i);
			final TopologyMaintenanceSession requestorSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(requestorAddress.location));

			// tell requester to accept responder
			requestorSession.acceptMe(node.getRemoteNodeAddress(), node.config.capabilities);
			peersToAccept--;
		}
	}

	public void acceptMe(final RemoteNodeAddress askerAddress, final Capabilities askerCapabilites) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} is asking to be accapted to {}", askerAddress, node.getRemoteNodeAddress());
		}

		node.peerManager.addByReplacement(askerAddress, askerCapabilites);
		if (!responder) {
			// we have accepted the responder now send them our capabilites so they can accept us
			final TopologyMaintenanceSession requestorSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(askerAddress.location));
			requestorSession.acceptMe(node.getRemoteNodeAddress(), node.config.capabilities);
		}
	}

	private int calcPeersToAccept(final List<RemoteNodeAddress> requesters) {
		return requesters.size() <= node.peerManager.getNumFreePeerSlots()
				? requesters.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}

	private static class FowardedRecentlyReset implements Runnable {
		@Override
		public void run() {
			fowardedRecently = false;
		}
	}
}
