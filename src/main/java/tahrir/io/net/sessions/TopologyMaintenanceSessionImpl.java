package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.tools.TrUtils;

import com.google.common.collect.Lists;

public class TopologyMaintenanceSessionImpl extends TrSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

	/**
	 * Nodes will not start their own maintenance if they have had to probe recently in order to avoid the
	 * network becoming flooded.
	 */
	private static boolean hasProbedRecently = false;

	private int peersToAccept;

	private boolean initator = false;


	/**
	 * A responder is the node where the search for a location ends, they will try accept as many
	 * forwarders as possible.
	 */
	private boolean responder = false;

	static {
		TrUtils.executor.scheduleWithFixedDelay(new HasProbedRecentlyReset(), 0, TrConstants.WAIT_FROM_PROBING_SEC, TimeUnit.SECONDS);
	}

	public TopologyMaintenanceSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
		logger = LoggerFactory.getLogger(TopologyMaintenanceSessionImpl.class.getName()+" ("+sessionId+")");
	}

	public void startTopologyMaintenance(final int locationToFind) {
		// TODO: check it hasn't done maintenance recently
		// this might be better in TrPeerManager
		logger.debug("Starting maintenance from {} with location {}", node.getRemoteNodeAddress(), locationToFind);

		initator = true;
		final List<RemoteNodeAddress> requesters = Lists.newLinkedList();
		final int hopsToLive = TrConstants.MAINTENANCE_HOPS_TO_LIVE;

		probeForLocation(locationToFind, hopsToLive, requesters);
	}

	public void probeForLocation(final int locationToFind, int hopsToLive, final List<RemoteNodeAddress> requesters) {
		hasProbedRecently = true;

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
			if (hopsToLive > TrConstants.MAINTENANCE_HOPS_TO_LIVE) {
				hopsToLive = TrConstants.HOPS_TO_LIVE_RESET;
			}

			if (!initator) {
				hopsToLive--;
				node.peerManager.updateTimeLastUsed(closestPeerAddress.location);
			}

			// add this node to the list of requesters
			requesters.add(node.getRemoteNodeAddress());

			// get next location
			final TopologyMaintenanceSession closestPeerSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(closestPeerAddress));
			closestPeerSession.probeForLocation(locationToFind, hopsToLive, requesters);
		}
	}

	public void sendResponses(final List<RemoteNodeAddress> requesters) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} is sending reponses to {}", node.getRemoteNodeAddress(), requesters);
		}

		while (peersToAccept > 0 && requesters.size() > 0) {
			final int randomRequester = TrUtils.rand.nextInt(requesters.size());
			final RemoteNodeAddress requesterAddress = requesters.remove(randomRequester);

			final TopologyMaintenanceSession requesterSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(requesterAddress));

			// tell requester to accept responder
			requesterSession.accept(node.getRemoteNodeAddress(), node.config.capabilities);
			peersToAccept--;
		}
	}

	public void accept(final RemoteNodeAddress addressToAccept, final Capabilities capabilitesToAccept) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} is asking to be accapted to {}", addressToAccept, node.getRemoteNodeAddress());
		}

		node.peerManager.addByReplacement(addressToAccept, capabilitesToAccept);
		if (!responder) {
			// we have accepted the responder now send them our capabilites so they can accept us
			final TopologyMaintenanceSession responderSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(sender()));
			responderSession.accept(node.getRemoteNodeAddress(), node.config.capabilities);
		}
	}

	private int calcPeersToAccept(final List<RemoteNodeAddress> requesters) {
		return requesters.size() <= node.peerManager.getNumFreePeerSlots()
				? requesters.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}

	public static int calcTopologyLoc(final RSAPublicKey publicKey) {
		return publicKey.hashCode();
	}

	private static class HasProbedRecentlyReset implements Runnable {
		@Override
		public void run() {
			hasProbedRecently = false;
		}
	}

	public static void enableDebugProbing() {
		TrUtils.executor.scheduleWithFixedDelay(new HasProbedRecentlyReset(), 0, 1, TimeUnit.SECONDS);
	}
}
