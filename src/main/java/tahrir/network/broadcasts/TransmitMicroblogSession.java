package tahrir.network.broadcasts;

import tahrir.TrConstants;
import tahrir.network.TrSession;
import tahrir.network.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.network.sessions.Priority;

public interface TransmitMicroblogSession extends TrSession {
	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void areYouInterested(final int mbHash);

	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void sendBroadcastMessage(final BroadcastMessage mb);

	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void interestIs(boolean interest);

	@Priority(TrConstants.MICROBLOG_BROADCAST_PRIORITY)
	public void sessionFinished();
}
