package tahrir.io.net;

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
	protected final TrSessionManager sessionMgr;
	private final ConcurrentLinkedQueue<Runnable> terminatedCallbacks = new ConcurrentLinkedQueue<Runnable>();
	private final Set<PhysicalNetworkLocation> toUnregister = Collections.synchronizedSet(new HashSet<PhysicalNetworkLocation>());
	private final String userLabel;

	public TrSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		userLabel = this.getClass().getName() + "(" + sessionId + ")";
		logger = LoggerFactory.getLogger(userLabel);
		this.sessionId = sessionId;
		this.node = node;
		this.sessionMgr = sessionMgr;
	}

	protected TrRemoteConnection connection(final TrPeerInfo peerInfo) {
		return connection(peerInfo.remoteNodeAddress, peerInfo.capabilities.allowsUnsolicitiedInbound);
	}

	public PhysicalNetworkLocation sender() {
		final PhysicalNetworkLocation r = sender.get();
		if (r == null)
			throw new RuntimeException("No sender stored in this thread.  Is this a local Session?  Are you calling getSender() from within a callback?  In either case, don't!");
		else
			return r;
	}

	protected final TrRemoteConnection connection(final PhysicalNetworkLocation physicalLocation) {
		return connection(new RemoteNodeAddress(physicalLocation, null), false);
	}

	protected final TrRemoteConnection connection(final PhysicalNetworkLocation physicalLocation, final boolean unilateral) {
		return connection(new RemoteNodeAddress(physicalLocation, null), unilateral);
	}

	protected final TrRemoteConnection connection(final RemoteNodeAddress address) {
		return connection(address, false);
	}

	protected final TrRemoteConnection connection(final RemoteNodeAddress address, final boolean unilateral) {
		toUnregister.add(address.physicalLocation);
		return sessionMgr.connectionManager.getConnection(address, unilateral, userLabel);
	}

	protected final TrRemoteConnection connectionWithUserLabel(final PhysicalNetworkLocation physicalLocation, final String label) {
		return connectionWithUserLabel(new RemoteNodeAddress(physicalLocation, null), false, label);
	}

	protected final TrRemoteConnection connectionWithUserLabel(final PhysicalNetworkLocation physicalLocation, final boolean unilateral, final String label) {
		return connectionWithUserLabel(new RemoteNodeAddress(physicalLocation, null), unilateral, label);
	}

	protected final TrRemoteConnection connectionWithUserLabel(final RemoteNodeAddress address, final String label) {
		return connectionWithUserLabel(address, false, label);
	}

	protected final TrRemoteConnection connectionWithUserLabel(final RemoteNodeAddress address, final boolean unilateral, final String label) {
		toUnregister.add(address.physicalLocation);
		return sessionMgr.connectionManager.getConnection(address, unilateral, label);
	}

	protected final <T extends TrSession> T remoteSession(final Class<T> cls,
			final TrRemoteConnection conn) {
		return remoteSession(cls, conn, sessionId);
	}

	protected final <T extends TrSession> T remoteSession(final Class<T> cls,
			final TrRemoteConnection conn,
			final int sessionId) {
		return sessionMgr.getOrCreateRemoteSession(cls, conn, sessionId);
	}

	public final void addTerminateCallback(final Runnable cb) {
		terminatedCallbacks.add(cb);
	}

	public void registerFailureListener(final Runnable listener) {
		throw new RuntimeException("registerFailureListner() can only be called on a remote session");
	}

	protected final void terminate() {
		for (final PhysicalNetworkLocation ra : toUnregister) {
			sessionMgr.connectionManager.noLongerNeeded(ra, userLabel);
		}
		for (final Runnable r : terminatedCallbacks) {
			r.run();
		}
		sessionMgr.sessions.remove(sessionId);
	}
}
