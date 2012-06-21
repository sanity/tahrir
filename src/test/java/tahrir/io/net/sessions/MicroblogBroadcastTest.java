package tahrir.io.net.sessions;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.TrNode;
import tahrir.tools.TrUtils;

public class MicroblogBroadcastTest {
	private static int port = 8888;

	@Test
	public void simpleTest() throws Exception {
		final TrNode sendingNode = TrUtils.makeTestNode(port++, false, false, false, true, 0, 1);
		final TrNode receivingNode = TrUtils.makeTestNode(port++, false, false, false, true, 0 , 1);

		final MicroblogHandler.Microblog testMb = new MicroblogHandler.Microblog(sendingNode, "Hello world");
		sendingNode.mbHandler.getMbQueue().insert(testMb);

		TrUtils.createTestBidirectionalConnection(sendingNode, receivingNode);

		sendingNode.mbHandler.setUpForBroadcast();

		for (int x=0; x<50; x++) {
			Thread.sleep(200);
			if (receivingNode.mbHandler.getMbQueue().contains(testMb)) {
				break;
			}
		}

		Assert.assertTrue(receivingNode.mbHandler.getMbQueue().contains(testMb));
	}
}
