package tahrir.io.net.sessions;

import java.io.File;

import org.testng.annotations.Test;

import tahrir.*;
import tahrir.TrNode.PublicNodeId;
import tahrir.tools.*;

public class AssimilateTest {
	@Test
	public void twoPeerTest() throws Exception {
		final TrConfig seedConfig = new TrConfig();
		seedConfig.capabilities.allowsAssimilation = true;
		seedConfig.capabilities.allowsUnsolicitiedInbound = true;
		seedConfig.peers.assimilate = false;
		final File seedDir = TrUtils.createTempDirectory();
		final TrNode seedNode = new TrNode(seedDir, seedConfig);

		final PublicNodeId seedPublicNodeId = seedNode.getPublicNodeId();

		final File joinerDir = TrUtils.createTempDirectory();

		final TrConfig joinerConfig = new TrConfig();

		final File joinerPubNodeIdsDir = new File(joinerDir, joinerConfig.publicNodeIdsDir);

		joinerPubNodeIdsDir.mkdir();

		Persistence.save(new File(joinerPubNodeIdsDir, "joiner-id"), seedPublicNodeId);

		seedConfig.capabilities.allowsAssimilation = false;
		seedConfig.capabilities.allowsUnsolicitiedInbound = false;
		seedConfig.peers.assimilate = true;
		final TrNode joinerNode = new TrNode(joinerDir, joinerConfig);

		Thread.sleep(10000);
	}
}
