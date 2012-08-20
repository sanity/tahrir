package tahrir.io.net.microblogging;

import org.testng.Assert;
import org.testng.annotations.*;

import tahrir.TrNode;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;
import tahrir.tools.TrUtils;

public class MicroblogBroadcastTest {
	private static int port = 8888;

	private TrNode sendingNode;
	private TrNode receivingNode;

	@BeforeTest
	public void setUpNodes() throws Exception {
		sendingNode = TrUtils.makeTestNode(port++, false, false, false, true, 1, 1);
		receivingNode = TrUtils.makeTestNode(port++, false, false, false, false, 1 , 1);
		TrUtils.createTestBidirectionalConnection(sendingNode, receivingNode);
		for (final TrPeerInfo pi :sendingNode.peerManager.peers.values()) {
			pi.capabilities.receivesMessageBroadcasts = true;
		}
	}

	@Test
	public void simpleTest() throws Exception {
		final MicroblogForBroadcast testMb = new MicroblogForBroadcast(sendingNode, "Hello world");
		sendingNode.mbManager.getMicroblogContainer().insert(testMb);

		// don't want initial wait
		sendingNode.mbManager.setupForNextMicroblog();

		for (int x=0; x<50; x++) {
			Thread.sleep(200);
			if (receivingNode.mbManager.getMicroblogContainer().contains(testMb)) {
				break;
			}
		}

		Assert.assertTrue(receivingNode.mbManager.getMicroblogContainer().contains(testMb), "Should contain the microblog");
	}

	@Test
	public void priorityTest() throws Exception {
		final MicroblogForBroadcast testMb0 = new MicroblogForBroadcast(sendingNode, "You SHOULD have this microblog!", 0);
		final MicroblogForBroadcast testMb1 = new MicroblogForBroadcast(sendingNode, "You should NOT have this microblog!", Integer.MAX_VALUE);
		sendingNode.mbManager.getMicroblogContainer().insert(testMb0);
		sendingNode.mbManager.getMicroblogContainer().insert(testMb1);

		// don't want initial wait
		sendingNode.mbManager.setupForNextMicroblog();

		for (int x=0; x<50; x++) {
			Thread.sleep(200);
		}

		Assert.assertTrue(receivingNode.mbManager.getMicroblogContainer().contains(testMb0), "Should contain the microblog with low priority");
		Assert.assertTrue(!receivingNode.mbManager.getMicroblogContainer().contains(testMb1), "Should not contain the microblog with high priority.");
	}
}
