package tahrir.io.net.sessions;

import java.io.File;

import org.testng.annotations.Test;

import tahrir.*;
import tahrir.io.net.TrPeerManager;
import tahrir.tools.*;

public class AssimilateTest {
	@Test(enabled=true)
	public void twoPeerTest() throws Exception {
		final TrConfig seedConfig = new TrConfig();
		seedConfig.capabilities.allowsAssimilation = true;
		seedConfig.capabilities.allowsUnsolicitiedInbound = true;
		seedConfig.peers.assimilate = false;
		seedConfig.localHostName = "localhost";
		seedConfig.udp.listenPort = 7643;
		final File seedDir = TrUtils.createTempDirectory();
		final TrNode seedNode = new TrNode(seedDir, seedConfig);

		final RemoteNodeAddress seedPublicNodeId = seedNode.getRemoteNodeAddress();

		final File joinerDir = TrUtils.createTempDirectory();

		final TrConfig joinerConfig = new TrConfig();

		joinerConfig.udp.listenPort = 7644;
		joinerConfig.peers.assimilate = true;

		final File joinerPubNodeIdsDir = new File(joinerDir, joinerConfig.publicNodeIdsDir);

		joinerPubNodeIdsDir.mkdir();

		// Ok, we should be getting this TrPeerInfo out of seedNode somehow rather
		// than needing to set its capabilities manually like this
		final TrPeerManager.TrPeerInfo seedPeerInfo = new TrPeerManager.TrPeerInfo(seedPublicNodeId);
		seedPeerInfo.capabilities = seedConfig.capabilities;

		Persistence.save(new File(joinerPubNodeIdsDir, "joiner-id"), seedPeerInfo);

		final TrNode joinerNode = new TrNode(joinerDir, joinerConfig);

		Thread.sleep(1000000);
	}
}
