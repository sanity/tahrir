package tahrir.io.net.microblogging;

import org.testng.Assert;
import org.testng.annotations.*;

import tahrir.TrNode;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.tools.TrUtils;

public class MicroblogBroadcastTest {
	private static int port = 8888;

	private TrNode sendingNode;
	private TrNode receivingNode;

	@BeforeTest
	public void setUpNodes() throws Exception {
		sendingNode = TrUtils.TestUtils.makeNode(port++, false, false, false, true, 1, 1);
		receivingNode = TrUtils.TestUtils.makeNode(port++, false, false, false, true, 1, 1);
		TrUtils.TestUtils.createBidirectionalConnection(sendingNode, receivingNode);
		for (final TrPeerInfo pi :sendingNode.peerManager.peers.values()) {
			pi.capabilities.receivesMessageBroadcasts = true;
		}
	}

	@Test
	public void simpleTest() throws Exception {
		final BroadcastMicroblog testMb = new BroadcastMicroblog(sendingNode, "Hello world");
		sendingNode.mbClasses.mbsForBroadcast.insert(testMb);

		// stop the receiver from broadcasting
		receivingNode.mbClasses.mbScheduler = null;

		// don't want initial wait
		sendingNode.mbClasses.mbScheduler.setupForNextMicroblog();
		// stop any more broadcasts
		sendingNode.mbClasses.mbScheduler = null;

		for (int x=0; x<50; x++) {
			Thread.sleep(20);
			if (receivingNode.mbClasses.mbsForBroadcast.contains(testMb)) {
				break;
			}
		}

		Assert.assertTrue(receivingNode.mbClasses.mbsForBroadcast.contains(testMb), "Should contain the microblog");
	}

	@Test
	public void priorityTest() throws Exception {
		final BroadcastMicroblog testMb0 = new BroadcastMicroblog(sendingNode, "You SHOULD have this microblog!", 0);
		final BroadcastMicroblog testMb1 = new BroadcastMicroblog(sendingNode, "You should NOT have this microblog!", Integer.MAX_VALUE);
		sendingNode.mbClasses.mbsForBroadcast.insert(testMb1);
		sendingNode.mbClasses.mbsForBroadcast.insert(testMb0);

		// stop the receiver from broadcasting
		receivingNode.mbClasses.mbScheduler = null;

		// don't want initial wait
		sendingNode.mbClasses.mbScheduler.setupForNextMicroblog();
		// stop any more broadcasts
		sendingNode.mbClasses.mbScheduler = null;

		for (int x=0; x<75; x++) {
			Thread.sleep(50);
			if (receivingNode.mbClasses.mbsForBroadcast.contains(testMb0)) {
				break;
			}
		}

		Assert.assertTrue(receivingNode.mbClasses.mbsForBroadcast.contains(testMb0), "Should contain the microblog with low priority");
		Assert.assertTrue(!receivingNode.mbClasses.mbsForBroadcast.contains(testMb1), "Should not contain the microblog with high priority.");
	}
}
