package tahrir.io.net;

import tahrir.TrNode;

public abstract class TrSessionImpl implements TrSession {

	public static final ThreadLocal<TrRemoteConnection<?>> sender = new ThreadLocal<TrRemoteConnection<?>>();

	protected final int sessionId;
	protected final TrNode<?> node;

	public TrSessionImpl(final Integer sessionId, final TrNode<?> node) {
		this.sessionId = sessionId;
		this.node = node;
	}

	/**
	 * Warning, this cannot be safely used in callbacks
	 * 
	 * @return
	 */
	public TrRemoteConnection<?> getSender() {
		return sender.get();
	}
}
