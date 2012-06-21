package tahrir.io.net.sessions;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.TrNode;
import tahrir.tools.TrUtils;

public class TopologyMaintenanceTest {
	private static int port = 8644;

	/**
	 * Nodes initially connected initiator<->forwarder1<->forwader2<->responder
	 */
	@Test
	public void smallWorldMaintenanceTest() throws Exception {
		final TrNode initiator = TrUtils.makeTestNode(port++, true, false, true, false, 4, 4);
		final TrNode forwarder1 = TrUtils.makeTestNode(port++, false, false, false, false, 4, 4);
		final TrNode forwarder2 = TrUtils.makeTestNode(port++, false, false, false, false, 4, 4);
		final TrNode responder = TrUtils.makeTestNode(port++, false, false, false, false, 4, 4);

		initiator.peerManager.locInfo.setLocation(0);
		forwarder1.peerManager.locInfo.setLocation(1);
		forwarder2.peerManager.locInfo.setLocation(2);
		responder.peerManager.locInfo.setLocation(3);

		TrUtils.createTestBidirectionalConnection(initiator, forwarder1);
		TrUtils.createTestBidirectionalConnection(forwarder1, forwarder2);
		TrUtils.createTestBidirectionalConnection(forwarder2, responder);

		initiator.peerManager.enableDebugMaintenance();

		for (int x=0; x<100; x++) {
			Thread.sleep(200);
			if (TrUtils.testIsConnected(initiator, responder) && TrUtils.testIsConnected (forwarder1, responder)) {
				break;
			}
		}

		Assert.assertTrue(TrUtils.testIsConnected(initiator, responder), "The initiator should be connected to responder");
		Assert.assertTrue(TrUtils.testIsConnected(forwarder1, responder), "The first forwarder should be connected to responder");
	}
}
