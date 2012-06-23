package tahrir.io.net.broadcast;

import org.testng.Assert;
import org.testng.annotations.*;

import tahrir.TrNode;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.broadcast.MicroblogHandler;
import tahrir.tools.TrUtils;

public class MicroblogBroadcastTest {
	private static int port = 8888;

	private TrNode sendingNode;
	private TrNode receivingNode;

	@BeforeTest
	public void setUpNodes() throws Exception {
		sendingNode = TrUtils.makeTestNode(port++, false, false, false, true, 1, 1);
		receivingNode = TrUtils.makeTestNode(port++, false, false, false, true, 1 , 1);
		TrUtils.createTestBidirectionalConnection(sendingNode, receivingNode);
		for (final TrPeerInfo pi :sendingNode.peerManager.peers.values()) {
			pi.capabilities.receivesMessageBroadcasts = true;
		}
	}

	@Test
	public void simpleTest() throws Exception {
		final MicroblogHandler.Microblog testMb = new MicroblogHandler.Microblog(sendingNode, "Hello world");
		sendingNode.mbHandler.getMbQueue().insert(testMb);

		// don't want initial wait
		sendingNode.mbHandler.setupForNextMicroblog();

		for (int x=0; x<50; x++) {
			Thread.sleep(200);
			if (receivingNode.mbHandler.getMbQueue().contains(testMb)) {
				break;
			}
		}

		Assert.assertTrue(receivingNode.mbHandler.getMbQueue().contains(testMb), "Should contain the microblog");
	}

	@Test
	public void priorityTest() throws Exception {
		final MicroblogHandler.Microblog testMb0 = new MicroblogHandler.Microblog(sendingNode, "You SHOULD have this microblog!", 0);
		final MicroblogHandler.Microblog testMb1 = new MicroblogHandler.Microblog(sendingNode, "You should NOT have this microblog!", Integer.MAX_VALUE);
		sendingNode.mbHandler.getMbQueue().insert(testMb0);
		sendingNode.mbHandler.getMbQueue().insert(testMb1);

		// don't want initial wait
		sendingNode.mbHandler.setupForNextMicroblog();

		for (int x=0; x<50; x++) {
			Thread.sleep(200);
		}

		Assert.assertTrue(receivingNode.mbHandler.getMbQueue().contains(testMb0), "Should contain the microblog with low priority");
		Assert.assertTrue(!receivingNode.mbHandler.getMbQueue().contains(testMb1), "Should not contain the microblog with high priority.");
	}
}
