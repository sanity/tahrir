package tahrir.io.net.microblogging;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

/**
 * Responsible for creating sessions for sending a single microblog between this node
 * and another if probabilistic conditions met.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogBroadcastSessionImpl extends TrSessionImpl implements MicroblogBroadcastSession {
	private static final Logger logger = LoggerFactory.getLogger(MicroblogBroadcastSessionImpl.class.getName());

	private MicroblogForBroadcast beingSent;

	private MicroblogBroadcastSession receiverSess;
	private MicroblogBroadcastSession initiatorSess;

	private boolean nextBroadcastStarted;

	private IncomingMicroblogHandler incomingMbHandler;

	public MicroblogBroadcastSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
	}

	public void startSingleBroadcast(final MicroblogForBroadcast mbToBroadcast, final PhysicalNetworkLocation peerPhysicalLoc) {
		nextBroadcastStarted = false;
		beingSent = mbToBroadcast;
		receiverSess = remoteSession(MicroblogBroadcastSession.class, connection(peerPhysicalLoc));
		receiverSess.registerFailureListener(new OnFailureRun());
		receiverSess.areYouInterested(beingSent.hashCode());
	}

	public void areYouInterested(final int mbHash) {
		initiatorSess = remoteSession(MicroblogBroadcastSession.class, connection(sender()));

		initiatorSess.interestIs(!node.mbManager.getMicroblogContainer().isLikelyToContain(mbHash));
	}

	public void interestIs(final boolean interest) {
		if (interest) {
			receiverSess.insertMicroblog(beingSent);
		} else {
			sessionFinished();
		}
	}

	public void insertMicroblog(final MicroblogForBroadcast mb) {
		incomingMbHandler.handleInsertion(mb);
		//node.mbManager.getMicroblogContainer().insert(mb);
		// TODO: this is a workaround until we have a registerSuccessListener()
		initiatorSess.sessionFinished();
	}

	public void sessionFinished() {
		startBroadcastToNextPeer();
	}

	private synchronized void startBroadcastToNextPeer() {
		if (!nextBroadcastStarted) {
			nextBroadcastStarted = true;
			node.mbManager.startBroadcastToPeer();
		}
	}

	private class OnFailureRun implements Runnable {
		public void run() {
			startBroadcastToNextPeer();
		}
	}
}
