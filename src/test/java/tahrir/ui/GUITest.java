package tahrir.ui;

import java.util.List;

import org.testng.collections.Lists;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.tools.TrUtils;

public class GUITest {
	public static void main(final String[] args) {
		try {
			final TrNode testNode = TrUtils.makeTestNode(9003, false, false, false, true, 0, 0);

			//UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

			final TrMainWindow mainWindow = new TrMainWindow(testNode);
			mainWindow.getContent().revalidate();
			GUITest.addDummyMicroblogs(testNode);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void addDummyMicroblogs(final TrNode node) {
		// create a set of dummy messages using the simple XML like format
		final String message0 = "<mb><txt>Ut non tellus at massa tincidunt malesuada sodales eu nunc. Proin in lectus sit amet mi tempor dictum. Nunc ullamcorper ornare metus eu ultrices. Maecenas vel erat id justo bibendum.</txt></mb>";
		final String message1 = "<mb><txt>Curabitur in elit in tortor viverra fringilla. In sed tincidunt.</txt></mb>";
		final String message2 = "<mb><txt>Suspendisse congue eleifend nunc sagittis euismod. Curabitur auctor nibh mauris, ut faucibus est. Class aptent taciti sociosqu ad litora torquent.</txt></mb>";
		final String message3 = "<mb><txt>Maecenas sed neque nisi. Suspendisse at velit urna, sed fermentum velit. Cum sociis natoque penatibus.</txt></mb>";
		final List<String> messages = Lists.newArrayList();
		messages.add(message0);
		messages.add(message1);
		messages.add(message2);
		messages.add(message3);

		// register the dummy microblogs with the test node so that they will display in GUI
		for (int i = 0; i < messages.size(); i++) {
			node.mbClasses.incomingMbHandler.handleInsertion(createTestMicroblog("userForMessage" + i, messages.get(i)));
		}
	}

	public static BroadcastMicroblog createTestMicroblog(final String nick, final String message) {
		return new BroadcastMicroblog(0, nick, TrCrypto.createRsaKeyPair().a, message, System.currentTimeMillis());
	}
}
