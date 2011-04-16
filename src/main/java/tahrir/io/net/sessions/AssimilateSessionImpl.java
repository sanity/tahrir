package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.udpV1.UdpRemoteAddress;

public class AssimilateSessionImpl extends TrSessionImpl implements AssimilateSession {

	public AssimilateSessionImpl(final Integer sessionId, final TrNode<?> node, final TrNet<?> trNet) {
		super(sessionId, node, trNet);
	}

	public void requestNewConnection(final UdpRemoteAddress requestor,
			final RSAPublicKey requestorPubkey) {
		final AssimilateSession senderAS = this.remoteSession(AssimilateSession.class, connection(sender()));
		if (node.peerManager.peers.size() < node.peerManager.config.maxPeers) {

		}
	}

}
