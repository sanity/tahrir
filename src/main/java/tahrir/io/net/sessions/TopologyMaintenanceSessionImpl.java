package tahrir.io.net.sessions;

import java.util.LinkedList;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.tools.TrUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Class for carrying out maintenance on topology in hopes of forming a small world network.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

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
		if (logger.isDebugEnabled()) {
			logger.debug("Starting maintenance from {} with locationToFind {}", node.getRemoteNodeAddress(), locationToFind);
		}

		initator = true;
		final LinkedList<RemoteNodeAddress> forwarders = Lists.newLinkedList();
		final int hopsToLive = TrConstants.MAINTENANCE_HOPS_TO_LIVE;

		probeForLocation(locationToFind, hopsToLive, forwarders);
	}

	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> forwarders) {
		final RemoteNodeAddress closestPeerAddress = node.peerManager.getClosestPeer(locationToFind);

		if (logger.isDebugEnabled()) {
			logger.debug("The closest peer found was {}, with a location of {}", node.getRemoteNodeAddress().physicalLocation, node.peerManager.locInfo.getLocation());
		}

		if (hopsToLive == 0 || closestPeerAddress.equals(node.getRemoteNodeAddress()) || (!initator && closestPeerAddress.physicalLocation.equals(sender()))) {
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
			}

			node.peerManager.updateTimeLastUsed(closestPeerAddress.physicalLocation);

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

			// check to see if connected
			if (!node.peerManager.peers.containsKey(requesterAddress.physicalLocation)) {
				final TopologyMaintenanceSession forwardersession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(requesterAddress));

				// tell requester to accept responder
				forwardersession.accept(node.getRemoteNodeAddress(), node.config.capabilities, node.peerManager.locInfo.getLocation());
				peersToAccept--;
			}
		}
	}

	public void accept(final RemoteNodeAddress addressToAccept, final Capabilities capabilitesToAccept, final int peerToAcceptLocation) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} is asking to be accepted to {}", addressToAccept, node.getRemoteNodeAddress());
		}

		node.peerManager.addByReplacement(addressToAccept, capabilitesToAccept);

		final TrPeerInfo accecptedPeerInfo = node.peerManager.peers.get(addressToAccept.physicalLocation);

		// check to see if topology location was set manually, if it was update the value from default
		if (peerToAcceptLocation != accecptedPeerInfo.topologyLocation) {
			node.peerManager.updatePeerInfo(addressToAccept.physicalLocation, new Function<TrPeerManager.TrPeerInfo, Void>() {

				public Void apply(final TrPeerInfo peerInfo) {
					peerInfo.topologyLocation = peerToAcceptLocation;
					return null;
				}
			});
		}

		if (!responder) {
			// we have accepted the responder now send them our capabilites so they can accept us
			final TopologyMaintenanceSession responderSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(sender()));
			responderSession.accept(node.getRemoteNodeAddress(), node.config.capabilities, node.peerManager.locInfo.getLocation());
		}
	}

	private int calcPeersToAccept(final LinkedList<RemoteNodeAddress> forwarders) {
		return forwarders.size() <= node.peerManager.getNumFreePeerSlots()
				? forwarders.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}
}
