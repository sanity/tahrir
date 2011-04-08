package tahrir.io.net.sessions;

import tahrir.TrNode;
import tahrir.io.net.*;

import com.google.common.base.Function;

public class AssimilateSessionImpl extends TrSessionImpl implements AssimilateSession {

	public AssimilateSessionImpl(final Integer sessionId, final TrNode<?> node, final TrNet<?> trNet) {
		super(sessionId, node, trNet);
	}

	public void findNewConnection(final TrRemoteConnection<?> introductoryPeerConnection,
			final Function<TrRemoteConnection<?>, Void> successCB) {
		final AssimilateSession introPeer = trNet.getOrCreateRemoteSession(AssimilateSessionImpl.class,
				introductoryPeerConnection, TrNetworkInterface.ASSIMILATION_PRIORITY);

	}

	public void requestAssimilation() {
		// TODO Auto-generated method stub

	}

}
