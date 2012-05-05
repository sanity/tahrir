package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.*;

import com.google.common.base.Function;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.tools.Persistence.Modified;
import tahrir.tools.Persistence.ModifyBlock;
import tahrir.tools.*;

public class AssimilateSessionImpl extends TrSessionImpl implements AssimilateSession {

	private final Logger logger;

	public static final long RELAY_ASSIMILATION_TIMEOUT_SECONDS = 60;
	private boolean locallyInitiated;
	private AssimilateSession pubNodeSession;
	private AssimilateSession receivedRequestFrom;
	private PhysicalNetworkLocation acceptorAddress;
	private RSAPublicKey acceptorPubkey;
	private Capabilities remoteCapabilities;
	private PhysicalNetworkLocation requestorAddress;
	private RSAPublicKey requestorPubkey;
	private long requestNewConnectionTime;
	private ScheduledFuture<?> requestNewConnectionFuture;
	private TrPeerInfo relay;

	public AssimilateSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
		logger = LoggerFactory.getLogger(AssimilateSessionImpl.class.getName()+" ("+sessionId+")");
	}

	public void startAssimilation(final Runnable onFailure, final TrPeerInfo assimilateVia) {
		logger.debug("Start assimilation via "+assimilateVia);
		relay = assimilateVia;
		requestNewConnectionTime = System.currentTimeMillis();
		requestNewConnectionFuture = TrUtils.executor.schedule(new Runnable() {

			public void run() {
				node.peerManager.updatePeerInfo(relay.remoteNodeAddress.location, new Function<TrPeerManager.TrPeerInfo, Void>() {

					public Void apply(final TrPeerInfo tpi) {
						if (logger.isDebugEnabled()) {
							logger.debug("Reporting assimilation failure for "+relay.remoteNodeAddress.location);
						}
						node.peerManager.reportAssimilationFailure(relay.remoteNodeAddress.location);
						return null;
					}
				});
			}
		}, RELAY_ASSIMILATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		locallyInitiated = true;
		pubNodeSession = this.remoteSession(AssimilateSession.class, this.connection(assimilateVia.remoteNodeAddress.location, assimilateVia.remoteNodeAddress.publicKey, true));
		pubNodeSession.registerFailureListener(onFailure);
		pubNodeSession.requestNewConnection(node.getRemoteNodeAddress().publicKey);
	}

	public void yourAddressIs(final PhysicalNetworkLocation address) {
		if (!locallyInitiated) {
			logger.warn("Received yourAddressIs() from {}, yet this AssimilateSession was not locally initiated",
					sender());
			return;
		}
		if (!sender().equals(relay.remoteNodeAddress.location)) {
			logger.warn("Received yourAddressIs() from {}, yet the public node we expected it from was {}, ignoring",
					sender(), relay.remoteNodeAddress.location);

			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug(sender()+" told us that our external address is "+address);
		}
		node.modifyPublicNodeId(new ModifyBlock<RemoteNodeAddress>() {

			public void run(final RemoteNodeAddress object, final Modified modified) {
				object.location = address;
			}
		});
	}


	public void requestNewConnection(final RSAPublicKey requestorPubkey) {
		requestNewConnection(null, requestorPubkey);
	}

	public void requestNewConnection(PhysicalNetworkLocation requestorAddress, final RSAPublicKey requestorPubkey) {
		final PhysicalNetworkLocation senderFV = sender();
		if (locallyInitiated) {
			logger.warn("Received requestNewConnection() from {}, but the session was locally initiated, ignoring",
					senderFV);
			return;
		}
		if (receivedRequestFrom != null) {
			logger.warn("Recieved another requestNewConnection() from {}, ignoring", senderFV);
			return;
		}
		receivedRequestFrom = this.remoteSession(AssimilateSession.class, connection(senderFV));
		if (requestorAddress == null) {
			receivedRequestFrom.yourAddressIs(senderFV);
			requestorAddress = senderFV;
		}
		this.requestorAddress = requestorAddress;
		this.requestorPubkey = requestorPubkey;
		if (node.peerManager.peers.size() < node.peerManager.config.maxPeers) {
			if (logger.isDebugEnabled()) {
				logger.debug("Accepting {} as new peer", requestorAddress);
			}
			// We're going to accept them
			final RemoteNodeAddress remoteNodeAddress = node.getRemoteNodeAddress();
			receivedRequestFrom.acceptNewConnection(remoteNodeAddress.location, remoteNodeAddress.publicKey);
			final AssimilateSession requestorSession = remoteSession(AssimilateSession.class,
					connection(this.requestorAddress, requestorPubkey, false));
			requestorSession.myCapabilitiesAre(node.config.capabilities);
		} else {
			relay = node.peerManager.getPeerForAssimilation();
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarding assimilation request from {} to {}", requestorAddress, relay);
			}

			requestNewConnectionTime = System.currentTimeMillis();
			requestNewConnectionFuture = TrUtils.executor.schedule(new Runnable() {

				public void run() {
					node.peerManager.updatePeerInfo(relay.remoteNodeAddress.location, new Function<TrPeerManager.TrPeerInfo, Void>() {

						public Void apply(final TrPeerInfo tpi) {
							if (logger.isDebugEnabled()) {
								logger.debug("Reporting assimilation failure to peerManager after sending assimilation request to {}", relay);
							}
							node.peerManager.reportAssimilationFailure(relay.remoteNodeAddress.location);
							return null;
						}
					});
				}
			}, RELAY_ASSIMILATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

			final AssimilateSession relaySession = remoteSession(AssimilateSession.class, connection(relay));

			relaySession.registerFailureListener(new Runnable() {

				public void run() {
					if (logger.isDebugEnabled()) {
						logger.debug("Reporting assimilation failure to peerManager after sending assimilation request to {}, and then trying again", relay);
					}
					node.peerManager.reportAssimilationFailure(relay.remoteNodeAddress.location);
					// Note: Important to use requestAddress field rather than
					// the parameter because the parameter may be null
					AssimilateSessionImpl.this.requestNewConnection(AssimilateSessionImpl.this.requestorAddress,
							requestorPubkey);
				}
			});

			relaySession.requestNewConnection(requestorAddress, requestorPubkey);
		}
	}

	public void acceptNewConnection(final PhysicalNetworkLocation acceptor, final RSAPublicKey acceptorPubkey) {
		if (!sender().equals(relay.remoteNodeAddress.location)) {
			logger.warn("Received acceptNewConnection() from {}, but was expecting it from {}", sender(), relay.remoteNodeAddress.location);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("{} is accepting assimiliation request", acceptor);
		}
		requestNewConnectionFuture.cancel(false);
		node.peerManager.updatePeerInfo(relay.remoteNodeAddress.location, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo tpi) {
				node.peerManager.reportAssimilationSuccess(relay.remoteNodeAddress.location, System.currentTimeMillis()
						- requestNewConnectionTime);
				return null;
			}
		});

		if (!locallyInitiated) {
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarding acceptance back to {}", requestorAddress);
			}
			receivedRequestFrom.acceptNewConnection(acceptor, acceptorPubkey);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("We initiated this assimiliation, add a new connection to {}", acceptor);
			}
			acceptorAddress = acceptor;
			this.acceptorPubkey = acceptorPubkey;
			addNewConnection();
		}
	}

	public void myCapabilitiesAre(final Capabilities myCapabilities) {
		if (locallyInitiated && (acceptorAddress != null && !sender().equals(acceptorAddress))) {
			logger.error("Received myCapabiltiesAre but not from acceptor, ignoring");
			return;
		}
		if (!locallyInitiated && !sender().equals(requestorAddress)) {
			logger.error("Received myCapabiltiesAre, but not from original requestor, ignoring");
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("{} informs us that it's capabilities are {}", acceptorAddress, myCapabilities);
		}
		remoteCapabilities = myCapabilities;
		addNewConnection();

	}

	private void addNewConnection() {
		if (acceptorAddress != null && remoteCapabilities != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding new connection to {}", acceptorAddress);
			}
			node.peerManager.addNewPeer(new RemoteNodeAddress(locallyInitiated ? acceptorAddress : requestorAddress,
					locallyInitiated ? acceptorPubkey : requestorPubkey), remoteCapabilities);
		}
	}
}