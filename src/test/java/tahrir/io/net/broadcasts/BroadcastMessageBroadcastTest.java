package tahrir.io.net.broadcasts;

import com.google.common.base.Optional;
import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.SignedBroadcastMessage;
import tahrir.tools.TrUtils;

public class BroadcastMessageBroadcastTest {

    @Test
    public void simpleTest() throws Exception {

        TrNode sendingNode = TrUtils.TestUtils.makeNode(8766, false, false, false, true, 1, 1);
        TrNode receivingNode = TrUtils.TestUtils.makeNode(8667, false, false, false, true, 1, 1);
        TrUtils.TestUtils.createBidirectionalConnection(sendingNode, receivingNode);
        for (final TrPeerInfo pi : sendingNode.peerManager.peers.values()) {
            pi.capabilities.receivesMessageBroadcasts = true;
        }
        sendingNode.config.currentUserIdentity = new UserIdentity("user1", TrCrypto.createRsaKeyPair().a, Optional.of(TrCrypto.createRsaKeyPair().b));

        final ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("Hello world", "en", sendingNode.mbClasses.identityStore);
        final SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, sendingNode.config.currentUserIdentity);
        final BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
        sendingNode.mbClasses.mbsForBroadcast.insert(broadcastMessage);

        // stop the receiver from broadcasting
        receivingNode.mbClasses.mbScheduler.disable();

        // Force it to run immediately
        sendingNode.mbClasses.mbScheduler.run();

        // stop any more broadcasts
        sendingNode.mbClasses.mbScheduler.disable();

        boolean a = receivingNode.mbClasses.mbsForBroadcast.contains(broadcastMessage);
        for (int x = 0; x < 50; x++) {
            Thread.sleep(20);
            if (receivingNode.mbClasses.mbsForBroadcast.contains(broadcastMessage)) {
                break;
            }
        }

        Assert.assertTrue(receivingNode.mbClasses.mbsForBroadcast.contains(broadcastMessage), "Should contain the microblog");
    }

    @Test
    public void priorityTest() throws Exception {
        TrNode sendingNode = TrUtils.TestUtils.makeNode(8769, false, false, false, true, 1, 1);
        TrNode receivingNode = TrUtils.TestUtils.makeNode(8645, false, false, false, true, 1, 1);
        TrUtils.TestUtils.createBidirectionalConnection(sendingNode, receivingNode);
        for (final TrPeerInfo pi : sendingNode.peerManager.peers.values()) {
            pi.capabilities.receivesMessageBroadcasts = true;
        }

        sendingNode.config.currentUserIdentity = new UserIdentity("user1", TrCrypto.createRsaKeyPair().a, Optional.of(TrCrypto.createRsaKeyPair().b));
        final ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("You SHOULD have this microblog!", "en", sendingNode.mbClasses.identityStore);
        final SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, sendingNode.config.currentUserIdentity);
        final BroadcastMessage broadcastMessage1 = new BroadcastMessage(signedBroadcastMessage);

        final ParsedBroadcastMessage parsedBroadcastMessage1 = ParsedBroadcastMessage.createFromPlaintext("You should NOT have this microblog!", "en", sendingNode.mbClasses.identityStore);
        final SignedBroadcastMessage signedBroadcastMessage1 = new SignedBroadcastMessage(parsedBroadcastMessage1, sendingNode.config.currentUserIdentity);
        final BroadcastMessage broadcastMessage2 = new BroadcastMessage(signedBroadcastMessage1);

        sendingNode.mbClasses.mbsForBroadcast.insert(broadcastMessage1);
        sendingNode.mbClasses.mbsForBroadcast.insert(broadcastMessage2);

        // stop the receiver from broadcasting
        receivingNode.mbClasses.mbScheduler.disable();

        // Force it to run immediately
        sendingNode.mbClasses.mbScheduler.run();

        // stop any more broadcasts
        sendingNode.mbClasses.mbScheduler.disable();

        for (int x = 0; x < 75; x++) {
            Thread.sleep(50);
            if (receivingNode.mbClasses.mbsForBroadcast.contains(broadcastMessage1)) {
                break;
            }
        }

        Assert.assertTrue(receivingNode.mbClasses.mbsForBroadcast.contains(broadcastMessage1), "Should contain the microblog with low priority");
        Assert.assertTrue(!receivingNode.mbClasses.mbsForBroadcast.contains(broadcastMessage2), "Should not contain the microblog with high priority.");
    }

}
