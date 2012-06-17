package tahrir.io.net.sessions;

import tahrir.io.net.*;
import tahrir.io.net.sessions.MicroblogBroadcastSessionImpl.Microblog;

public interface MicoblogBroadcastSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void insertMicroblog(final Microblog mb);
}
