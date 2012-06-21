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

	private MicoblogBroadcastSession initiatorSess;

	public MicroblogBroadcastSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
	}

	public void startSingleBroadcast(final Microblog mbToBroadcast, final PhysicalNetworkLocation peerPhysicalLoc) {
		beingSent = mbToBroadcast;
		receiverSess = remoteSession(MicoblogBroadcastSession.class, connection(peerPhysicalLoc));
		receiverSess.areYouInterested(beingSent.hashCode());
		receiverSess.registerFailureListener(new OnFailureRun());
	}

	public void areYouInterested(final int mbHash) {
		initiatorSess = remoteSession(MicoblogBroadcastSession.class, connection(sender()));

		initiatorSess.interested(!node.mbHandler.getMbQueue().isLikelyToContain(mbHash));
	}

	public void interested(final boolean interest) {
		if (interest) {
			receiverSess.insertMicroblog(beingSent);
		} else {
			// TODO: this is a workaround until we have a registerSuccessListener
			// the dummy parameter is also a workaround for until no param RPC works
			final byte dummyParameter = 0;
			receiverSess.sessionFinished(dummyParameter);
		}
	}

	public void insertMicroblog(final Microblog mb) {
		node.mbHandler.getMbQueue().insert(mb);
		// TODO: this is a workaround until we have a registerSuccessListener
		// the dummy parameter is also a workaround for until no param RPC works
		final byte dummyParameter = 0;
		initiatorSess.sessionFinished(dummyParameter);
	}

	public void sessionFinished(final byte dummyParam) {
		finished();
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
