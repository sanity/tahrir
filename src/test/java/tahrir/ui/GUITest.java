package tahrir.ui;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.microblogs.Microblog;
import tahrir.tools.TrUtils;

public class GUITest {
	public static void main(final String[] args) {
		try {
			final TrNode testNode = TrUtils.makeTestNode(9003, false, false, false, true, 0, 0);

			//UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

			final TrMainWindow mainWindow = new TrMainWindow(testNode);
			GUITest.addDummyMicroblogs(testNode);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void addDummyMicroblogs(final TrNode node) {
		final Microblog testMb0 = new Microblog(node, "<mb><txt>Ut non tellus at massa tincidunt malesuada sodales eu nunc. Proin in lectus sit amet mi tempor dictum. Nunc ullamcorper ornare metus eu ultrices. Maecenas vel erat id justo bibendum.</txt></mb>");
		final Microblog testMb1 = new Microblog(node, "<mb><txt>Curabitur in elit in tortor viverra fringilla. In sed tincidunt.</txt></mb>");
		final Microblog testMb2 = new Microblog(node, "<mb><txt>Suspendisse congue eleifend nunc sagittis euismod. Curabitur auctor nibh mauris, ut faucibus est. Class aptent taciti sociosqu ad litora torquent.</txt></mb>");
		final Microblog testMb3 = new Microblog(node, "<mb><txt>Maecenas sed neque nisi. Suspendisse at velit urna, sed fermentum velit. Cum sociis natoque penatibus.</txt></mb>");

		testMb0.authorNick = "chuck-norris";
		testMb1.authorNick = "sanity";
		testMb2.authorNick = "nomel";
		testMb3.authorNick = "bubbles";
		testMb3.publicKey = TrCrypto.createRsaKeyPair().a;

		node.mbClasses.incomingMbHandler.handleInsertion(testMb0);
		node.mbClasses.incomingMbHandler.handleInsertion(testMb1);
		node.mbClasses.incomingMbHandler.handleInsertion(testMb2);
		node.mbClasses.incomingMbHandler.handleInsertion(testMb3);
	}
}
