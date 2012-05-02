package tahrir.io.net;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.TrPeerManager.TrPeerInfo;

public abstract class TrSessionImpl implements TrSession {
	protected Logger logger;
	public static final ThreadLocal<PhysicalNetworkLocation> sender = new ThreadLocal<PhysicalNetworkLocation>();
	protected final int sessionId;
	protected final TrNode node;
	protected final TrSessionManager trNet;
	private final ConcurrentLinkedQueue<Runnable> terminatedCallbacks = new ConcurrentLinkedQueue<Runnable>();

	public TrSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager trNet) {
		userLabel = this.getClass().getName() + "(" + sessionId + ")";
		logger = LoggerFactory.getLogger(userLabel);
		this.sessionId = sessionId;
		this.node = node;
		this.trNet = trNet;
	}

	protected TrRemoteConnection connection(final TrPeerInfo peerInfo) {
		return connection(peerInfo.remoteNodeAddress.location, peerInfo.remoteNodeAddress.publicKey, peerInfo.capabilities.allowsUnsolicitiedInbound);
	}

	public PhysicalNetworkLocation sender() {
		final PhysicalNetworkLocation r = sender.get();
		if (r == null)
			throw new RuntimeException("No sender stored in this thread.  Is this a local Session?  Are you calling getSender() from within a callback?  In either case, don't!");
		else
			return r;
	}

	private final Set<PhysicalNetworkLocation> toUnregister = Collections.synchronizedSet(new HashSet<PhysicalNetworkLocation>());
	private final String userLabel;

	protected final TrRemoteConnection connection(final PhysicalNetworkLocation address) {
		return connection(address, null, false);
	}

	protected final TrRemoteConnection connection(final PhysicalNetworkLocation address, final boolean unilateral) {
		return connection(address, null, unilateral);
	}

	protected final TrRemoteConnection connection(final PhysicalNetworkLocation address,
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
		for (final PhysicalNetworkLocation ra : toUnregister) {
			trNet.connectionManager.noLongerNeeded(ra, userLabel);
		}
		for (final Runnable r : terminatedCallbacks) {
			r.run();
		}
		trNet.sessions.remove(sessionId);
	}
}
