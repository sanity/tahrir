package tahrir.io.net.sessions;

import tahrir.io.net.TrSession;

public interface Ping extends TrSession {
	public void ping(Ping sender);

	public void pong(Ping sender);
}
