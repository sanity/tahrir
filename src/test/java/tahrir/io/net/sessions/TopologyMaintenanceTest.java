package tahrir.io.net.sessions;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.*;
import tahrir.tools.TrUtils;

public class TopologyMaintenanceTest {
	private static int port = 8644;

	/**
	 * Nodes initially connected initiator<->forwarder1<->forwader2<->responder
	 */
	@Test
	public void smallWorldMaintenanceTest() throws Exception {
		final TrNode initiator = makeNode(true);
		final TrNode forwarder1 = makeNode(false);
		final TrNode forwarder2 = makeNode(false);
		final TrNode responder = makeNode(false);

		initiator.peerManager.locInfo.setLocation(0);
		forwarder1.peerManager.locInfo.setLocation(1);
		forwarder2.peerManager.locInfo.setLocation(2);
		responder.peerManager.locInfo.setLocation(3);

		createBidirectionalConnection(initiator, forwarder1);
		createBidirectionalConnection(forwarder1, forwarder2);
		createBidirectionalConnection(forwarder2, responder);

		initiator.peerManager.enableDebugMaintenance();

		for (int x=0; x<100; x++) {
			Thread.sleep(200);
			if (isConnected(initiator, responder) && isConnected (forwarder1, responder)) {
				break;
			}
		}

		Assert.assertTrue(isConnected(initiator, responder), "The initiator should be connected to responder");
		Assert.assertTrue(isConnected(forwarder1, responder), "The first forwarder should be connected to responder");
	}

	private boolean isConnected(final TrNode node1, final TrNode node2) {
		return node1.peerManager.peers.containsKey(node2.getRemoteNodeAddress().physicalLocation)
				&& node2.peerManager.peers.containsKey(node1.getRemoteNodeAddress().physicalLocation);
	}

	private TrNode makeNode(final boolean initiator) throws Exception {
		final File nodeDir = TrUtils.createTempDirectory();

		final TrConfig nodeConfig = new TrConfig();

		nodeConfig.udp.listenPort = port++;
		nodeConfig.localHostName = "127.0.0.1";
		nodeConfig.peers.runMaintainance = initiator;
		nodeConfig.peers.assimilate = false;
		nodeConfig.peers.topologyMaintenance = initiator;

		final File joinerPubNodeIdsDir = new File(nodeDir, nodeConfig.publicNodeIdsDir);

		joinerPubNodeIdsDir.mkdir();

		return new TrNode(nodeDir, nodeConfig);
	}

	private void createBidirectionalConnection(final TrNode node1, final TrNode node2) {
		node1.peerManager.addNewPeer(node2.getRemoteNodeAddress(), node2.config.capabilities, node2.peerManager.locInfo.getLocation());
		node2.peerManager.addNewPeer(node1.getRemoteNodeAddress(), node1.config.capabilities, node1.peerManager.locInfo.getLocation());
	}
}
