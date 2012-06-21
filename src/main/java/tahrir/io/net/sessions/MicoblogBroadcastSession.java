package tahrir.io.net.sessions;

import tahrir.io.net.*;
import tahrir.io.net.sessions.MicroblogHandler.Microblog;

public interface MicoblogBroadcastSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void areYouInterested(final int mbHash);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void insertMicroblog(final Microblog mb);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void interested(boolean interest);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY) // TODO: change priority
	public void sessionFinished(byte dummyParam);
}
