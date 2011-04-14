package tahrir.io.net;

import java.util.concurrent.ConcurrentLinkedQueue;

import tahrir.TrNode;

public abstract class TrSessionImpl implements TrSession {

	public static final ThreadLocal<TrRemoteConnection<?>> sender = new ThreadLocal<TrRemoteConnection<?>>();
	protected final int sessionId;
	protected final TrNode<?> node;
	protected final TrNet<?> trNet;
	private final ConcurrentLinkedQueue<Runnable> terminatedCallbacks = new ConcurrentLinkedQueue<Runnable>();

	public TrSessionImpl(final Integer sessionId, final TrNode<?> node, final TrNet<?> trNet) {
		this.sessionId = sessionId;
		this.node = node;
		this.trNet = trNet;
	}

	public TrRemoteConnection<?> getSender() {
		final TrRemoteConnection<?> r = sender.get();
		if (r == null)
			throw new RuntimeException("No sender stored in this thread.  Is this a local Session?  Are you calling getSender() from within a callback?  In either case, don't!");
		else
			return r;
	}

	public void addTerminateCallback(final Runnable cb) {
		terminatedCallbacks.add(cb);
	}

	public void terminate() {
		for (final Runnable r : terminatedCallbacks) {
			r.run();
		}
		trNet.sessions.remove(sessionId);
	}
}
