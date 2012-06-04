package tahrir.io.net.sessions;

import java.io.File;
import java.util.List;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.udpV1.UdpNetworkLocation;
import tahrir.tools.*;

import com.google.common.collect.Lists;

public class LongTests {
	public static void main(final String[] args) throws Exception {
		final LongTests test = new LongTests();
		test.start();
	}

	public void start() throws Exception {
		final TrConfig seedConfig = new TrConfig();
		seedConfig.capabilities.allowsAssimilation = true;
		seedConfig.capabilities.allowsUnsolicitiedInbound = true;
		seedConfig.peers.assimilate = false;
		seedConfig.localHostName = "localhost";
		seedConfig.udp.listenPort = 8048;
		final File seedDir = TrUtils.createTempDirectory();
		final TrNode seedNode = new TrNode(seedDir, seedConfig);
		final RemoteNodeAddress seedPublicNodeId = seedNode.getRemoteNodeAddress();

		final List<TrNode> joiners = Lists.newLinkedList();

		for (int x=0; x<150; x++) {
			Thread.sleep(500);
			final File joinerDir = TrUtils.createTempDirectory();

			final TrConfig joinerConfig = new TrConfig();

			joinerConfig.udp.listenPort = 8050+x;
			joinerConfig.localHostName = "localhost";
			joinerConfig.peers.assimilate = true;
			final File joinerPubNodeIdsDir = new File(joinerDir, joinerConfig.publicNodeIdsDir);

			joinerPubNodeIdsDir.mkdir();

			// Ok, we should be getting this TrPeerInfo out of seedNode somehow rather
			// than needing to set its capabilities manually like this
			final TrPeerManager.TrPeerInfo seedPeerInfo = new TrPeerManager.TrPeerInfo(seedPublicNodeId);
			seedPeerInfo.capabilities = seedConfig.capabilities;

			Persistence.save(new File(joinerPubNodeIdsDir, "joiner-id"), seedPeerInfo);

			final TrNode node = new TrNode(joinerDir, joinerConfig);
			node.peerManager.enableDebugMaintenance();
			joiners.add(node);
		}

		Thread.sleep(20000);

		dumpVertex(seedNode);
		for (final TrNode joiner : joiners) {
			dumpVertex(joiner);
		}
	}

	private void dumpVertex(final TrNode node) {
		for (final PhysicalNetworkLocation o : node.peerManager.peers.keySet()) {
			System.out.print(((UdpNetworkLocation)node.getRemoteNodeAddress().location).port + " -- "+((UdpNetworkLocation) o).port+"; ");
		}
	}
}
