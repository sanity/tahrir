package tahrir.io.net.sessions;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.sessions.MicroblogHandler.Microblog;

/**
 * Responsible for creating sessions for sending a single microblog between this node
 * and another if probabilistic conditions met.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogBroadcastSessionImpl extends TrSessionImpl implements MicoblogBroadcastSession {
	private static final Logger logger = LoggerFactory.getLogger(MicroblogBroadcastSessionImpl.class.getName());

	private Microblog beingSent;
	private MicoblogBroadcastSession receiverSess;

	public MicroblogBroadcastSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
	}

	public void startSingleBroadcast(final Microblog mbToBroadcast, final PhysicalNetworkLocation peerPhysicalLoc) {
		beingSent = mbToBroadcast;
		receiverSess = remoteSession(MicoblogBroadcastSession.class, connection(peerPhysicalLoc));
		receiverSess.areYouInterested(beingSent.hashCode());
	}

	public void areYouInterested(final int mbHash) {
		final MicoblogBroadcastSession senderSess = remoteSession(MicoblogBroadcastSession.class, connection(sender()));
		senderSess.registerFailureListener(new OnFailureRun());

		if (!node.mbHandler.getMbQueue().isLikelyToContain(mbHash)) {
			senderSess.yesInterested();
		} else {
			senderSess.noInterest();
		}
	}

	public void yesInterested() {
		receiverSess.insertMicroblog(beingSent);
		finished();
	}

	public void noInterest() {
		finished();
	}

	public void insertMicroblog(final Microblog mb) {
		node.mbHandler.getMbQueue().insert(mb);
	}

	private void finished() {
		node.mbHandler.startNext();
	}

	private class OnFailureRun implements Runnable {
		public void run() {
			finished();
		}
	}
}
