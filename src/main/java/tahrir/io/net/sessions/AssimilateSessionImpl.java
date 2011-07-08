package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.*;

import tahrir.*;
import tahrir.TrNode.PublicNodeId;
import tahrir.io.net.*;
import tahrir.peerManager.*;
import tahrir.peerManager.TrPeerManager.Capabilities;
import tahrir.peerManager.TrPeerManager.TrPeerInfo;
import tahrir.tools.Persistence.Modified;
import tahrir.tools.Persistence.ModifyBlock;
import tahrir.tools.*;

import com.google.common.base.Function;

public class AssimilateSessionImpl extends TrSessionImpl implements AssimilateSession {
	public static final long RELAY_ASSIMILATION_TIMEOUT_SECONDS = 60;
	private boolean locallyInitiated;
	private AssimilateSession pubNodeSession;
	private AssimilateSession receivedRequestFrom;
	private TrRemoteAddress acceptorAddress;
	private RSAPublicKey acceptorPubkey;
	private Capabilities remoteCapabilities;
	private TrRemoteAddress requestorAddress;
	private RSAPublicKey requestorPubkey;
	private TrRemoteAddress assimilateViaAddress;
	private long requestNewConnectionTime;
	private ScheduledFuture<?> requestNewConnectionFuture;
	private TrPeerInfo relay;

	public AssimilateSessionImpl(final Integer sessionId, final TrNode node, final TrNet trNet) {
		super(sessionId, node, trNet);
	}

	public void startAssimilation(final Runnable onFailure, final TrRemoteConnection connection) {
		assimilateViaAddress = connection.getRemoteAddress();
		locallyInitiated = true;
		pubNodeSession = this.remoteSession(AssimilateSession.class, connection);
		pubNodeSession.registerFailureListener(onFailure);
		pubNodeSession.requestNewConnection(node.getPublicNodeId().publicKey);
	}

	public void yourAddressIs(final TrRemoteAddress address) {
		if (!locallyInitiated) {
			logger.warn("Received yourAddressIs() from {}, yet this AssimilateSession was not locally initiated",
					sender());
			return;
		}
		if (!address.equals(assimilateViaAddress)) {
			logger.warn("Received yourAddressIs() from {}, yet the public node we expected it from was {}, ignoring",
					sender(), assimilateViaAddress);
			return;
		}
		logger.info("{} told us that our address is {}", sender(), address);
		node.modifyPublicNodeId(new ModifyBlock<TrNode.PublicNodeId>() {

			public void run(final PublicNodeId object, final Modified modified) {
				object.address = address;
			}
		});
	}


	public void requestNewConnection(final RSAPublicKey requestorPubkey) {
		requestNewConnection(null, requestorPubkey);
	}

	public void requestNewConnection(final TrRemoteAddress requestorAddress, final RSAPublicKey requestorPubkey) {
		final TrRemoteAddress senderFV = sender();
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
		}
		this.requestorAddress = requestorAddress == null ? senderFV : requestorAddress;
		this.requestorPubkey = requestorPubkey;
		if (node.peerManager.peers.size() < node.peerManager.config.maxPeers) {
			// We're going to accept them
			final PublicNodeId publicNodeId = node.getPublicNodeId();
			receivedRequestFrom.acceptNewConnection(publicNodeId.address, publicNodeId.publicKey);
			final AssimilateSession requestorSession = remoteSession(AssimilateSession.class,
					connection(requestorAddress, requestorPubkey, false));
			requestorSession.myCapabilitiesAre(node.config.capabilities);
		} else {
			relay = node.peerManager.getPeerForAssimilation();

			requestNewConnectionTime = System.currentTimeMillis();
			requestNewConnectionFuture = TrUtils.executor.schedule(new Runnable() {

				public void run() {
					node.peerManager.updatePeerInfo(relay.addr, new Function<TrPeerManager.TrPeerInfo, Void>() {

						public Void apply(final TrPeerInfo tpi) {
							node.peerManager.reportAssimilationFailure(relay.addr);
							return null;
						}
					});
				}
			}, RELAY_ASSIMILATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

			final AssimilateSession relaySession = remoteSession(AssimilateSession.class, connection(relay));

			relaySession.registerFailureListener(new Runnable() {

				public void run() {
					node.peerManager.reportAssimilationFailure(relay.addr);
					// Note: Important to use requestAddress field rather than
					// the parameter because the parameter may be null
					AssimilateSessionImpl.this.requestNewConnection(AssimilateSessionImpl.this.requestorAddress,
							requestorPubkey);
				}
			});

			relaySession.requestNewConnection(requestorAddress, requestorPubkey);
		}
	}

	public void acceptNewConnection(final TrRemoteAddress acceptor, final RSAPublicKey acceptorPubkey) {
		if (!sender().equals(relay)) {
			logger.warn("Received acceptNewConnection() from {}, but was expecting it from {}", sender(), relay);
		}
		requestNewConnectionFuture.cancel(false);
		node.peerManager.updatePeerInfo(relay.addr, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo tpi) {
				node.peerManager.reportAssimilationSuccess(relay.addr, System.currentTimeMillis()
						- requestNewConnectionTime);
				return null;
			}
		});

		if (!locallyInitiated) {
			receivedRequestFrom.acceptNewConnection(acceptor, acceptorPubkey);
		} else {
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
		remoteCapabilities = myCapabilities;
		addNewConnection();

	}

	private void addNewConnection() {
		if (acceptorAddress != null && remoteCapabilities != null) {
			node.peerManager.addNewPeer(locallyInitiated ? acceptorAddress : requestorAddress,
					locallyInitiated ? acceptorPubkey : requestorPubkey, remoteCapabilities);
		}
	}
}