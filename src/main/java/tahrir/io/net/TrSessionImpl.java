package tahrir.io.net;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.peerManager.TrPeerManager.TrPeerInfo;

public abstract class TrSessionImpl implements TrSession {
	protected Logger logger;
	public static final ThreadLocal<TrRemoteAddress> sender = new ThreadLocal<TrRemoteAddress>();
	protected final int sessionId;
	protected final TrNode node;
	protected final TrNet trNet;
	private final ConcurrentLinkedQueue<Runnable> terminatedCallbacks = new ConcurrentLinkedQueue<Runnable>();

	public TrSessionImpl(final Integer sessionId, final TrNode node, final TrNet trNet) {
		userLabel = this.getClass().getName() + "(" + sessionId + ")";
		logger = LoggerFactory.getLogger(userLabel);
		this.sessionId = sessionId;
		this.node = node;
		this.trNet = trNet;
	}

	protected TrRemoteConnection connection(final TrPeerInfo peerInfo) {
		return connection(peerInfo.publicNodeId.address, peerInfo.publicNodeId.publicKey, peerInfo.capabilities.allowsUnsolicitiedInbound);
	}

	public TrRemoteAddress sender() {
		final TrRemoteAddress r = sender.get();
		if (r == null)
			throw new RuntimeException("No sender stored in this thread.  Is this a local Session?  Are you calling getSender() from within a callback?  In either case, don't!");
		else
			return r;
	}

	private final Set<TrRemoteAddress> toUnregister = Collections.synchronizedSet(new HashSet<TrRemoteAddress>());
	private final String userLabel;

	protected final TrRemoteConnection connection(final TrRemoteAddress address) {
		return connection(address, null, false);
	}

	protected final TrRemoteConnection connection(final TrRemoteAddress address,
			final RSAPublicKey pubKey,
			final boolean unilateral) {
		toUnregister.add(address);
		return trNet.connectionManager.getConnection(address, pubKey, unilateral, userLabel);
	}

	protected final <T extends TrSession> T remoteSession(final Class<T> cls,
			final TrRemoteConnection conn) {
		return remoteSession(cls, conn, sessionId);
	}

	protected final <T extends TrSession> T remoteSession(final Class<T> cls,
			final TrRemoteConnection conn,
			final int sessionId) {
		return trNet.getOrCreateRemoteSession(cls, conn, sessionId);
	}

	public final void addTerminateCallback(final Runnable cb) {
		terminatedCallbacks.add(cb);
	}

	public void registerFailureListener(final Runnable listener) {
		throw new RuntimeException("registerFailureListner() can only be called on a remote session");
	}

	protected final void terminate() {
		for (final TrRemoteAddress ra : toUnregister) {
			trNet.connectionManager.noLongerNeeded(ra, userLabel);
		}
		for (final Runnable r : terminatedCallbacks) {
			r.run();
		}
		trNet.sessions.remove(sessionId);
	}
}
