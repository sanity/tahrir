package tahrir.ui;

import javax.swing.UIManager;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;
import tahrir.tools.TrUtils;

public class GUITest {
	public static void main(final String[] args) {
		try {
			final TrNode testNode = TrUtils.makeTestNode(9003, false, false, false, true, 0, 0);
			GUITest.addDummyMicroblogs(testNode);

			UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

			final TrMainWindow mainWindow = new TrMainWindow(testNode);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void addDummyMicroblogs(final TrNode node) {
		final MicroblogForBroadcast testMb0 = new MicroblogForBroadcast(node, "Ut non tellus at massa tincidunt malesuada sodales eu nunc. Proin in lectus sit amet mi tempor dictum. Nunc ullamcorper ornare metus eu ultrices. Maecenas vel erat id justo bibendum.");
		final MicroblogForBroadcast testMb1 = new MicroblogForBroadcast(node, "Curabitur in elit in tortor viverra fringilla. In sed tincidunt.");
		final MicroblogForBroadcast testMb2 = new MicroblogForBroadcast(node, "Suspendisse congue eleifend nunc sagittis euismod. Curabitur auctor nibh mauris, ut faucibus est. Class aptent taciti sociosqu ad litora torquent.");
		final MicroblogForBroadcast testMb3 = new MicroblogForBroadcast(node, "Maecenas sed neque nisi. Suspendisse at velit urna, sed fermentum velit. Cum sociis natoque penatibus.");

		testMb0.authorNick = "chuck-norris";
		testMb1.authorNick = "sanity";
		testMb2.authorNick = "nomel";
		testMb3.authorNick = "bubbles";
		testMb3.publicKey = TrCrypto.createRsaKeyPair().a;

		node.mbManager.getMicroblogContainer().insert(testMb0);
		node.mbManager.getMicroblogContainer().insert(testMb1);
		node.mbManager.getMicroblogContainer().insert(testMb2);
		node.mbManager.getMicroblogContainer().insert(testMb3);

	}
}
