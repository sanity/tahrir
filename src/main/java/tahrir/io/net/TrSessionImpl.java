package tahrir.io.net;

import tahrir.TrNode;

public abstract class TrSessionImpl implements TrSession {

	public static final ThreadLocal<TrRemoteConnection<?>> sender = new ThreadLocal<TrRemoteConnection<?>>();

	public static final ThreadLocal<TrNet<?>> trNet = new ThreadLocal<TrNet<?>>();

	protected final int sessionId;
	protected final TrNode<?> node;

	public TrSessionImpl(final Integer sessionId, final TrNode<?> node) {
		this.sessionId = sessionId;
		this.node = node;
	}

	public TrRemoteConnection<?> getSender() {
		return sender.get();
	}

	public TrNet<?> getTrNet() {
		return trNet.get();
	}
}
