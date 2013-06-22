package tahrir.io.net.microblogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrNode;
import tahrir.io.net.PhysicalNetworkLocation;
import tahrir.io.net.TrSessionImpl;
import tahrir.io.net.TrSessionManager;
import tahrir.io.net.microblogging.microblogs.Microblog;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A session for broadcasting a microblog to a node.
 * <p/>
 * The microblog will be broadcast if a node expresses interest based
 * on a probalistic condition otherwise the session will end.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class TransmitMicroblogSessionImpl extends TrSessionImpl implements TransmitMicroblogSession {
    private static final Logger logger = LoggerFactory.getLogger(TransmitMicroblogSessionImpl.class.getName());

    private Microblog microblogToSend;

    private TransmitMicroblogSession receiverSess;
    private TransmitMicroblogSession initiatorSess;

    private CountDownLatch transmissionCompleteLatch;

    public TransmitMicroblogSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
        super(sessionId, node, sessionMgr);
    }

    public void attemptToSendMicroblogAndWaitUntilComplete(final Microblog microblogToSend, final PhysicalNetworkLocation recepient) {
        this.microblogToSend = microblogToSend;
        receiverSess = remoteSession(TransmitMicroblogSession.class, connection(recepient));
        receiverSess.registerFailureListener(new Runnable() {
            @Override
            public void run() {
                sessionFinished();
            }
        });
        receiverSess.areYouInterested(this.microblogToSend.hashCode());
        this.transmissionCompleteLatch = new CountDownLatch(1);
        try {
            if (!this.transmissionCompleteLatch.await(1, TimeUnit.MINUTES)) {
                logger.warn("Microblog broadcast timed out");
            }
        } catch (InterruptedException e) {
            logger.error("Latch interrupted while awaiting broadcast completion");
        }

    }

    public void areYouInterested(final int mbHash) {
        initiatorSess = remoteSession(TransmitMicroblogSession.class, connection(sender()));

        initiatorSess.interestIs(!node.mbClasses.mbsForBroadcast.isLikelyToContain(mbHash));
    }

    public void interestIs(final boolean interest) {
        if (interest) {
            receiverSess.sendMicroblog(microblogToSend);
        } else {
            sessionFinished();
        }
    }

    public void sendMicroblog(final Microblog mb) {
        node.mbClasses.incomingMbHandler.handleInsertion(mb);
        initiatorSess.sessionFinished();
    }

    public void sessionFinished() {
        if (transmissionCompleteLatch != null) transmissionCompleteLatch.countDown();
    }

}
