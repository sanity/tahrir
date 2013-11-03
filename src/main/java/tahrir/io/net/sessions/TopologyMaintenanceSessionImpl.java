package tahrir.io.net.sessions;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.tools.TrUtils;

import java.util.LinkedList;

/**
 * Class for carrying out maintenance on topology in hopes of forming a small world network.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class TopologyMaintenanceSessionImpl implements TopologyMaintenanceSession {
	private final Logger logger;

    private final TrSessionImpl session;
    private final TrNode node;

    private boolean initiator = false;

	private PhysicalNetworkLocation receivedProbeFrom;

	private LinkedList<RemoteNodeAddress> willConnectTo;

	/**
	 * A acceptor is the node where the search for a location ends, they will try accept as many
	 * forwarders as possible.
	 */
	private boolean acceptor = false;
	private RemoteNodeAddress acceptorAddress;

	public TopologyMaintenanceSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
        this.node = node;
		session = new TrSessionImpl(sessionId, node, sessionMgr);
		logger = LoggerFactory.getLogger(TopologyMaintenanceSessionImpl.class.getName()+" ("+sessionId+")");
	}

	public TopologyMaintenanceSessionImpl(final Integer sessionId, final TrNode node, final TrSessionImpl session) {
        this.node = node;
		this.session = session;
		logger = LoggerFactory.getLogger(TopologyMaintenanceSessionImpl.class.getName()+" ("+sessionId+")");
	}

    public void startTopologyMaintenance(final int locationToFind) {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting maintenance from {} with locationToFind {}", node.getRemoteNodeAddress(), locationToFind);
		}

		initiator = true;
		final LinkedList<RemoteNodeAddress> forwarders = Lists.newLinkedList();
		final int hopsToLive = TrConstants.MAINTENANCE_HOPS_TO_LIVE;

		probeForLocation(locationToFind, hopsToLive, forwarders);
	}

	public void probeForLocation(final int locationToFind, int hopsToLive, final LinkedList<RemoteNodeAddress> forwarders) {
		if (!initiator) {
			receivedProbeFrom = session.sender();
			hopsToLive--;
			node.getPeerManager().hasForwardedRecently = true;
		}

		final RemoteNodeAddress closestPeerAddress = node.getPeerManager().getClosestPeer(locationToFind);

		if (logger.isDebugEnabled()) {
			logger.debug("The closest peer found was {}, with a location of {}", node.getRemoteNodeAddress().physicalLocation, node.getPeerManager().getLocInfo().getLocation());
		}

		if (hopsToLive == 0 || closestPeerAddress.equals(node.getRemoteNodeAddress()) || (!initiator && closestPeerAddress.physicalLocation.equals(session.sender()))) {
			// the current node is the closest to what we're looking for or we've given up
			acceptor = true;
			sendResponses(forwarders);
		} else {
			if (hopsToLive > TrConstants.MAINTENANCE_HOPS_TO_LIVE) {
				hopsToLive = TrConstants.HOPS_TO_LIVE_RESET;
			}

			node.getPeerManager().updateTimeLastUsed(closestPeerAddress.physicalLocation);

			forwarders.add(node.getRemoteNodeAddress());

			// get next location
			final TopologyMaintenanceSession closestPeerSession = session.remoteSession(TopologyMaintenanceSession.class, closestPeerAddress);
			closestPeerSession.probeForLocation(locationToFind, hopsToLive, forwarders);
		}
	}

	private void sendResponses(final LinkedList<RemoteNodeAddress> forwarders) {
		int peersToAccept = getNumPeerToAccept(forwarders);

		willConnectTo = Lists.newLinkedList();

		// get nodes we're going to connect to
		while (peersToAccept > 0 && forwarders.size() > 0) {
			final int randomNum = TrUtils.rand.nextInt(forwarders.size());
			final RemoteNodeAddress randomForwarder = forwarders.remove(randomNum);

			// check to see if connected
			if (!node.getPeerManager().peers.containsKey(randomForwarder.physicalLocation)) {
				willConnectTo.add(randomForwarder);
				peersToAccept--;
			}
		}

		// trace back so that then can accept us
		if (!initiator && willConnectTo.size() > 0) {
            final TopologyMaintenanceSession senderSess = session.remoteSession(TopologyMaintenanceSession.class, receivedProbeFrom);
            senderSess.sendAcceptInfo(node.getRemoteNodeAddress(), willConnectTo);
		}

		// tell them we're trying to connect
		for (final RemoteNodeAddress nodeToConnect : willConnectTo) {

			final TopologyMaintenanceSession forwarderSess = session.remoteSession(TopologyMaintenanceSession.class, nodeToConnect);
			forwarderSess.myCapabilitiesAre(node.getConfig().capabilities, node.getPeerManager().getLocInfo().getLocation());
		}
	}

    private int backOff(int attempt){
        return ((2^attempt - 1)/2);
    }

	public void sendAcceptInfo(final RemoteNodeAddress acceptor, final LinkedList<RemoteNodeAddress> willConnectTo) {
		if (willConnectTo.contains(node.getRemoteNodeAddress())) {
			final TopologyMaintenanceSession acceptorSess = session.remoteSession(TopologyMaintenanceSession.class, acceptor);
			acceptorAddress = acceptor;
			acceptorSess.myCapabilitiesAre(node.getConfig().capabilities, node.getPeerManager().getLocInfo().getLocation());
		}

		if (!initiator) {
			final TopologyMaintenanceSession senderSess = session.remoteSession(TopologyMaintenanceSession.class, receivedProbeFrom);
			senderSess.sendAcceptInfo(acceptor, willConnectTo);
		}
	}

	public void myCapabilitiesAre(final Capabilities myCapabilities, final int topologyLocation) {
		if (!acceptor) {
			node.getPeerManager().addByReplacement(acceptorAddress, myCapabilities, topologyLocation);
		} else {
			RemoteNodeAddress forwarderAddress = null;
			// find the remote address corresponding to sender()
			for (RemoteNodeAddress address : willConnectTo) {
				if (address.physicalLocation.equals(session.sender())) {
					forwarderAddress = address;
					break;
				}
			}
			node.getPeerManager().addByReplacement(forwarderAddress, myCapabilities, topologyLocation);
		}
	}

    @Override
    public void registerFailureListener(Runnable listener) {
        session.registerFailureListener(listener);
    }

    @Override
    public void terminate() {
        session.terminate();
    }

    private int getNumPeerToAccept(final LinkedList<RemoteNodeAddress> forwarders) {
		return forwarders.size() <= node.getPeerManager().getNumFreePeerSlots()
				? forwarders.size()
						: TrConstants.TOPOLOGY_MAINTENANCE_PEERS_TO_REPLACE;
	}
}
