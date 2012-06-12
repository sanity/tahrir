package tahrir.io.net.sessions;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.*;
import tahrir.tools.TrUtils;

public class TopologyMaintenanceTest {
	private static int port = 8644;

	/**
	 * Nodes initially connected initiator<->fowarder1<->fowarder2<->responder
	 */
	@Test
	public void smallWorldMaintenanceTest() throws Exception {
		final TrNode forwarder = makeNode(false);
		final TrNode initiator1 = makeNode(true);
		final TrNode forwarder2 = makeNode(false);

		System.out.println("initiator1 has a location of " + forwarder.peerManager.locInfo.getLocation());
		System.out.println("forwarder has a location of " + initiator1.peerManager.locInfo.getLocation());
		System.out.println("initiator2 has a location of " + forwarder2.peerManager.locInfo.getLocation());

		createBidirectionalConnection(forwarder2, forwarder);
		createBidirectionalConnection(initiator1, forwarder);

		initiator1.peerManager.enableDebugMaintenance();

		for (int x=0; x<200000000; x++) {
			Thread.sleep(500);
			if (isConnected(initiator1, forwarder2) || !isConnected(forwarder, initiator1)) {
				break;
			}
		}

		Assert.assertTrue(isConnected(initiator1, forwarder2), "The initiator should be connected to forwarder2");
	}

	private boolean isConnected(final TrNode node1, final TrNode node2) {
		return node1.peerManager.peers.containsKey(node2.getRemoteNodeAddress().physicalLocation)
				&& node2.peerManager.peers.containsKey(node1.getRemoteNodeAddress().physicalLocation);
	}

	private TrNode makeNode(final boolean initiator) throws Exception {
		final File nodeDir = TrUtils.createTempDirectory();

		final TrConfig nodeConfig = new TrConfig();

		nodeConfig.udp.listenPort = port++;
		nodeConfig.localHostName = "localhost";
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
