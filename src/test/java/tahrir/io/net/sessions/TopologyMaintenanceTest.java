package tahrir.io.net.sessions;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.TrNode;
import tahrir.tools.TrUtils;

public class TopologyMaintenanceTest {
	private static int port = 8112;

	/**
	 * Nodes initially connected initiator<->forwarder1<->forwader2<->responder
	 */
	@Test
	public void smallWorldMaintenanceTest() throws Exception {
		final TrNode initiator = TrUtils.TestUtils.makeNode(port++, true, false, true, false, 4, 4);
		final TrNode forwarder1 = TrUtils.TestUtils.makeNode(port++, false, false, false, false, 4, 4);
		final TrNode forwarder2 = TrUtils.TestUtils.makeNode(port++, false, false, false, false, 4, 4);
		final TrNode responder = TrUtils.TestUtils.makeNode(port++, false, false, false, false, 4, 4);

		initiator.getPeerManager().locInfo.setLocation(0);
		forwarder1.getPeerManager().locInfo.setLocation(1);
		forwarder2.getPeerManager().locInfo.setLocation(2);
		responder.getPeerManager().locInfo.setLocation(3);

		TrUtils.TestUtils.createBidirectionalConnection(initiator, forwarder1);
		TrUtils.TestUtils.createBidirectionalConnection(forwarder1, forwarder2);
		TrUtils.TestUtils.createBidirectionalConnection(forwarder2, responder);

		initiator.getPeerManager().enableDebugMaintenance();

		for (int x=0; x<100; x++) {
			Thread.sleep(200);
			if (TrUtils.TestUtils.isConnected(initiator, responder) && TrUtils.TestUtils.isConnected(forwarder1, responder)) {
				break;
			}
		}

		Assert.assertTrue(TrUtils.TestUtils.isConnected(initiator, responder), "The initiator should be connected to responder");
		Assert.assertTrue(TrUtils.TestUtils.isConnected(forwarder1, responder), "The first forwarder should be connected to responder");
	}
}
