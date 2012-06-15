package tahrir.io.net.sessions;

import java.util.LinkedList;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.tools.TrUtils;

import com.google.common.collect.Lists;

/**
 * Class for carrying out maintenance on topology in hopes of forming a small world network.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class TopologyMaintenanceSessionImpl extends TrSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

	private boolean initator = false;

	private PhysicalNetworkLocation receivedProbeFrom;

	private LinkedList<RemoteNodeAddress> willConnectTo;

	/**
	 * A acceptor is the node where the search for a location ends, they will try accept as many
	 * forwarders as possible.
	 */
	private boolean acceptor = false;
	private RemoteNodeAddress acceptorAddress;

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
		if (!initator) {
			receivedProbeFrom = sender();
			hopsToLive--;
			node.peerManager.hasForwardedRecenlty = true;
		}

		final RemoteNodeAddress closestPeerAddress = node.peerManager.getClosestPeer(locationToFind);

		if (logger.isDebugEnabled()) {
			logger.debug("The closest peer found was {}, with a location of {}", node.getRemoteNodeAddress().physicalLocation, node.peerManager.locInfo.getLocation());
		}

		if (hopsToLive == 0 || closestPeerAddress.equals(node.getRemoteNodeAddress()) || (!initator && closestPeerAddress.physicalLocation.equals(sender()))) {
			// the current node is the closest to what we're looking for or we've given up
			acceptor = true;
			sendResponses(forwarders);
		} else {
			if (hopsToLive > TrConstants.MAINTENANCE_HOPS_TO_LIVE) {
				hopsToLive = TrConstants.HOPS_TO_LIVE_RESET;
			}

			node.peerManager.updateTimeLastUsed(closestPeerAddress.physicalLocation);

			forwarders.add(node.getRemoteNodeAddress());

			// get next location
			final TopologyMaintenanceSession closestPeerSession = this.remoteSession(TopologyMaintenanceSession.class, this.connection(closestPeerAddress));
			closestPeerSession.probeForLocation(locationToFind, hopsToLive, forwarders);
		}
	}

	public void sendResponses(final LinkedList<RemoteNodeAddress> forwarders) {
		int peersToAccept = getNumPeerToAccept(forwarders);

		willConnectTo = Lists.newLinkedList();

		// get nodes we're going to connect to
		while (peersToAccept > 0 && forwarders.size() > 0) {
			final int randomNum = TrUtils.rand.nextInt(forwarders.size());
			final RemoteNodeAddress randomForwarder = forwarders.remove(randomNum);

			// check to see if connected
			if (!node.peerManager.peers.containsKey(randomForwarder.physicalLocation)) {
				willConnectTo.add(randomForwarder);
				peersToAccept--;
			}
		}

		// trace back so that then can accept us
		if (!initator && willConnectTo.size() > 0) {
			final TopologyMaintenanceSession senderSess = this.remoteSession(TopologyMaintenanceSession.class, connection(receivedProbeFrom));
			senderSess.sendAcceptInfo(node.getRemoteNodeAddress(), willConnectTo);
		}

		// tell them we're trying to connect
		for (final RemoteNodeAddress nodeToConnect : willConnectTo) {
			final TopologyMaintenanceSession forwarderSess = this.remoteSession(TopologyMaintenanceSession.class, connection(nodeToConnect));
			forwarderSess.myCapabilitiesAre(node.config.capabilities, node.peerManager.locInfo.getLocation());
		}
	}

	public void sendAcceptInfo(final RemoteNodeAddress acceptor, final LinkedList<RemoteNodeAddress> willConnectTo) {
		if (willConnectTo.contains(node.getRemoteNodeAddress())) {
			final TopologyMaintenanceSession acceptorSess = this.remoteSession(TopologyMaintenanceSession.class, connection(acceptor));
			acceptorAddress = acceptor;
			acceptorSess.myCapabilitiesAre(node.config.capabilities, node.peerManager.locInfo.getLocation());
		}

		if (!initator) {
			final TopologyMaintenanceSession senderSess = this.remoteSession(TopologyMaintenanceSession.class, connection(receivedProbeFrom));
			senderSess.sendAcceptInfo(acceptor, willConnectTo);
		}
	}

	public void myCapabilitiesAre(final Capabilities myCapabilities, final int topologyLocation) {
		if (!acceptor) {
			node.peerManager.addByReplacement(acceptorAddress, myCapabilities, topologyLocation);
		} else {
			RemoteNodeAddress forwarderAddress = null;
			// find the remote address corresponding to sender()
			for (int i = 0; i < willConnectTo.size(); i++) {
				final RemoteNodeAddress address = willConnectTo.get(i);
				if (address.physicalLocation.equals(sender())) {
					forwarderAddress = address;
					break;
				}
			}
			node.peerManager.addByReplacement(forwarderAddress, myCapabilities, topologyLocation);
		}
	}

	private int getNumPeerToAccept(final LinkedList<RemoteNodeAddress> forwarders) {
		return forwarders.size() <= node.peerManager.getNumFreePeerSlots()
				? forwarders.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}
}
