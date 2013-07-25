package tahrir.io.net.microblogging;

import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.TrNode;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.microblogging.microblogs.BroadcastMessage;
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

        final BroadcastMessage testMb = new BroadcastMessage(sendingNode, "<mb>Hello world</mb>");
        sendingNode.mbClasses.mbsForBroadcast.insert(testMb);

        // stop the receiver from broadcasting
        receivingNode.mbClasses.mbScheduler.disable();

        // Force it to run immediately
        sendingNode.mbClasses.mbScheduler.run();

        // stop any more broadcasts
        sendingNode.mbClasses.mbScheduler.disable();

        for (int x = 0; x < 50; x++) {
            Thread.sleep(20);
            if (receivingNode.mbClasses.mbsForBroadcast.contains(testMb)) {
                break;
            }
        }

        Assert.assertTrue(receivingNode.mbClasses.mbsForBroadcast.contains(testMb), "Should contain the microblog");
    }

    @Test
    public void priorityTest() throws Exception {
        TrNode sendingNode = TrUtils.TestUtils.makeNode(8769, false, false, false, true, 1, 1);
        TrNode receivingNode = TrUtils.TestUtils.makeNode(8645, false, false, false, true, 1, 1);
        TrUtils.TestUtils.createBidirectionalConnection(sendingNode, receivingNode);
        for (final TrPeerInfo pi : sendingNode.peerManager.peers.values()) {
            pi.capabilities.receivesMessageBroadcasts = true;
        }


        final BroadcastMessage testMb0 = new BroadcastMessage(sendingNode, "<mb>You SHOULD have this microblog!</mb>", 0);
        final BroadcastMessage testMb1 = new BroadcastMessage(sendingNode, "<mb>You should NOT have this microblog!</mb>", Integer.MAX_VALUE);
        sendingNode.mbClasses.mbsForBroadcast.insert(testMb1);
        sendingNode.mbClasses.mbsForBroadcast.insert(testMb0);

        // stop the receiver from broadcasting
        receivingNode.mbClasses.mbScheduler.disable();

        // Force it to run immediately
        sendingNode.mbClasses.mbScheduler.run();

        // stop any more broadcasts
        sendingNode.mbClasses.mbScheduler.disable();

        for (int x = 0; x < 75; x++) {
            Thread.sleep(50);
            if (receivingNode.mbClasses.mbsForBroadcast.contains(testMb0)) {
                break;
            }
        }

        Assert.assertTrue(receivingNode.mbClasses.mbsForBroadcast.contains(testMb0), "Should contain the microblog with low priority");
        Assert.assertTrue(!receivingNode.mbClasses.mbsForBroadcast.contains(testMb1), "Should not contain the microblog with high priority.");
    }

}
