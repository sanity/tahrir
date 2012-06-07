package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;
import java.util.LinkedList;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.tools.TrUtils;

import com.google.common.collect.Lists;

public class TopologyMaintenanceSessionImpl extends TrSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

	private int peersToAccept;

	private boolean initator = false;

	/**
	 * A responder is the node where the search for a location ends, they will try accept as many
	 * forwarders as possible.
	 */
	private boolean responder = false;

	public TopologyMaintenanceSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
		logger = LoggerFactory.getLogger(TopologyMaintenanceSessionImpl.class.getName()+" ("+sessionId+")");
	}

	public void startTopologyMaintenance(final int locationToFind) {
		logger.debug("Starting maintenance from {} with location {}", node.getRemoteNodeAddress(), locationToFind);

		initator = true;
		final LinkedList<RemoteNodeAddress> forwarders = Lists.newLinkedList();
		final int hopsToLive = TrConstants.MAINTENANCE_HOPS_TO_LIVE;

		probeForLocation(locationToFind, hopsToLive, forwarders);
	}

	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> forwarders) {
		final RemoteNodeAddress closestPeerAddress = node.peerManager.getClosestPeer(locationToFind);

		if (logger.isDebugEnabled()) {
			logger.debug("The closest peer found was {}", closestPeerAddress);
		}

		if (hopsToLive == 0 || closestPeerAddress.equals(node.getRemoteNodeAddress())) {
			// the current node is the closest to what we're looking for or we've given up
			responder = true;
			peersToAccept = calcPeersToAccept(forwarders);
			sendResponses(forwarders);
		} else {
			if (hopsToLive > TrConstants.MAINTENANCE_HOPS_TO_LIVE) {
				hopsToLive = TrConstants.HOPS_TO_LIVE_RESET;
			}

			if (!initator) {
				hopsToLive--;
				node.peerManager.hasForwardedRecenlty = true;
				node.peerManager.updateTimeLastUsed(closestPeerAddress.location);
			}

			forwarders.add(node.getRemoteNodeAddress());

			// get next location
			final TopologyMaintenanceSession closestPeerSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(closestPeerAddress));
			closestPeerSession.probeForLocation(locationToFind, hopsToLive, forwarders);
		}
	}

	public void sendResponses(final LinkedList<RemoteNodeAddress> forwarders) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} is sending reponses to {}", node.getRemoteNodeAddress(), forwarders);
		}

		while (peersToAccept > 0 && forwarders.size() > 0) {
			final int randomRequester = TrUtils.rand.nextInt(forwarders.size());
			final RemoteNodeAddress requesterAddress = forwarders.remove(randomRequester);

			final TopologyMaintenanceSession forwardersession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(requesterAddress));

			// tell requester to accept responder
			forwardersession.accept(node.getRemoteNodeAddress(), node.config.capabilities);
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

	private int calcPeersToAccept(final LinkedList<RemoteNodeAddress> forwarders) {
		return forwarders.size() <= node.peerManager.getNumFreePeerSlots()
				? forwarders.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}

	public static int calcTopologyLoc(final RSAPublicKey publicKey) {
		return Math.abs(publicKey.hashCode());
	}
}
