package tahrir.io.net.sessions;

import tahrir.TrNode;

public class PingImpl implements Ping {

	private final int sessionId;

	public PingImpl(final int sessionId, final TrNode node) {
		this.sessionId = sessionId;
	}

	public void ping(final Ping sender) {
		sender.pong(this);
	}

	public void pong(final Ping sender) {
		// NOOP
	}

	public int getSessionId() {
		return sessionId;
	}

	public SessionType getSessionType() {
		return SessionType.LOCAL;
	}

}
