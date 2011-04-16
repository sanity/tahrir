package tahrir.io.net;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import tahrir.TrNode;

public abstract class TrSessionImpl implements TrSession {

	public static final ThreadLocal<TrRemoteAddress> sender = new ThreadLocal<TrRemoteAddress>();
	protected final int sessionId;
	protected final TrNode node;
	protected final TrNet trNet;
	private final ConcurrentLinkedQueue<Runnable> terminatedCallbacks = new ConcurrentLinkedQueue<Runnable>();

	public TrSessionImpl(final Integer sessionId, final TrNode node, final TrNet trNet) {
		this.sessionId = sessionId;
		this.node = node;
		this.trNet = trNet;
		userLabel = this.getClass().getName() + "(" + sessionId + ")";
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

	public final TrRemoteConnection connection(final TrRemoteAddress address) {
		return connection(address, null, false);
	}

	public final TrRemoteConnection connection(final TrRemoteAddress address,
			final RSAPublicKey pubKey,
			final boolean unilateral) {
		toUnregister.add(address);
		return trNet.connectionManager.getConnection(address, pubKey, unilateral, userLabel);
	}

	public final <T extends TrSession> T remoteSession(final Class<T> cls,
 final TrRemoteConnection conn) {
		return remoteSession(cls, conn, sessionId);
	}

	public final <T extends TrSession> T remoteSession(final Class<T> cls,
 final TrRemoteConnection conn,
			final int sessionId) {
		return trNet.getOrCreateRemoteSession(cls, conn, sessionId);
	}

	public final void addTerminateCallback(final Runnable cb) {
		terminatedCallbacks.add(cb);
	}

	public final void terminate() {
		for (final TrRemoteAddress ra : toUnregister) {
			trNet.connectionManager.noLongerNeeded(ra, userLabel);
		}
		for (final Runnable r : terminatedCallbacks) {
			r.run();
		}
		trNet.sessions.remove(sessionId);
	}
}
