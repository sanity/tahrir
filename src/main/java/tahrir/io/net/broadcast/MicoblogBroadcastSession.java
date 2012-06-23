package tahrir.io.net.broadcast;

import tahrir.TrConstants;
import tahrir.io.net.TrSession;
import tahrir.io.net.broadcast.MicroblogHandler.Microblog;
import tahrir.io.net.sessions.*;

public interface MicoblogBroadcastSession extends TrSession {
	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void areYouInterested(final int mbHash);

	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void insertMicroblog(final Microblog mb);

	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void interestIs(boolean interest);

	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void sessionFinished();
}
