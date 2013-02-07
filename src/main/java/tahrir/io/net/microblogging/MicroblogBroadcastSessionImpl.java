package tahrir.io.net.microblogging;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;

/**
 * A session for broadcasting a microblog to a node.
 * 
 * The microblog will be broadcast if a node expresses interest based
 * on a probalistic condition otherwise the session will end.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogBroadcastSessionImpl extends TrSessionImpl implements MicroblogBroadcastSession {
	private static final Logger logger = LoggerFactory.getLogger(MicroblogBroadcastSessionImpl.class.getName());

	private BroadcastMicroblog beingSent;

	private MicroblogBroadcastSession receiverSess;
	private MicroblogBroadcastSession initiatorSess;

	private boolean nextBroadcastStarted;

	public MicroblogBroadcastSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
	}

	public void startSingleBroadcast(final BroadcastMicroblog mbToBroadcast, final PhysicalNetworkLocation peerPhysicalLoc) {
		nextBroadcastStarted = false;
		beingSent = mbToBroadcast;
		receiverSess = remoteSession(MicroblogBroadcastSession.class, connection(peerPhysicalLoc));
		receiverSess.registerFailureListener(new OnFailureRun());
		receiverSess.areYouInterested(beingSent.hashCode());
	}

	public void areYouInterested(final int mbHash) {
		initiatorSess = remoteSession(MicroblogBroadcastSession.class, connection(sender()));

		initiatorSess.interestIs(!node.mbClasses.mbsForBroadcast.isLikelyToContain(mbHash));
	}

	public void interestIs(final boolean interest) {
		if (interest) {
			receiverSess.insertMicroblog(beingSent);
		} else {
			sessionFinished();
		}
	}

	public void insertMicroblog(final BroadcastMicroblog mb) {
		node.mbClasses.incomingMbHandler.handleInsertion(mb);
		// TODO: this is a workaround until we have a registerSuccessListener()
		initiatorSess.sessionFinished();
	}

	public void sessionFinished() {
		startBroadcastToNextPeer();
	}

	private synchronized void startBroadcastToNextPeer() {
		if (!nextBroadcastStarted) {
			nextBroadcastStarted = true;
			node.mbClasses.mbScheduler.startBroadcastToPeer();
		}
	}

	private class OnFailureRun implements Runnable {
		public void run() {
			startBroadcastToNextPeer();
		}
	}
}
