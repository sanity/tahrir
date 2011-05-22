package tahrir.io.net.sessions;

import java.io.File;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.*;
import tahrir.TrNode.PublicNodeId;
import tahrir.TrNode.PublicNodeIdInfo;
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

		final PublicNodeIdInfo seedNodeInfo = new PublicNodeIdInfo();
		seedNodeInfo.id = seedPublicNodeId;

		final File joinerDir = TrUtils.createTempDirectory();

		final TrConfig joinerConfig = new TrConfig();

		final File joinerPubNodeIdsDir = new File(joinerDir, joinerConfig.publicNodeIdsDir);

		joinerPubNodeIdsDir.mkdir();

		Persistence.save(new File(joinerPubNodeIdsDir, "joiner-id"), seedNodeInfo);

		seedConfig.capabilities.allowsAssimilation = false;
		seedConfig.capabilities.allowsUnsolicitiedInbound = false;
		seedConfig.peers.assimilate = true;
		final TrNode joinerNode = new TrNode(joinerDir, joinerConfig);

		final ArrayList<File> pubNodeFiles = joinerNode.getPublicNodeIdFiles();

		Assert.assertEquals(pubNodeFiles.size(), 1, "Ensure that the joiner has the seed node as a public node");

		joinerNode.peerManager.maintainance();

		Thread.sleep(10000);
	}
}
