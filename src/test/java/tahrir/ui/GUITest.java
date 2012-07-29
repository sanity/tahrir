package tahrir.ui;

import javax.swing.UIManager;

import tahrir.TrNode;
import tahrir.io.net.microblogging.MicrobloggingManger;
import tahrir.tools.TrUtils;

public class GUITest {

	public static void main(final String[] args) {
		TrMainWindow mainWindow;
		try {
			final TrNode testNode = TrUtils.makeTestNode(9012, false, false, false, true, 0, 0);
			GUITest.addDummyMicroblogs(testNode);

			UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

			mainWindow = new TrMainWindow(testNode);
			mainWindow.setVisible(true);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void addDummyMicroblogs(final TrNode node) {
		final MicrobloggingManger.Microblog testMb0 = new MicrobloggingManger.Microblog(node, "Ut non tellus at massa tincidunt malesuada sodales eu nunc. Proin in lectus sit amet mi tempor dictum. Nunc ullamcorper ornare metus eu ultrices. Maecenas vel erat id justo bibendum.");
		final MicrobloggingManger.Microblog testMb1 = new MicrobloggingManger.Microblog(node, "Curabitur in elit in tortor viverra fringilla. In sed tincidunt.");
		final MicrobloggingManger.Microblog testMb2 = new MicrobloggingManger.Microblog(node, "Suspendisse congue eleifend nunc sagittis euismod. Curabitur auctor nibh mauris, ut faucibus est. Class aptent taciti sociosqu ad litora torquent.");
		final MicrobloggingManger.Microblog testMb3 = new MicrobloggingManger.Microblog(node, "Maecenas sed neque nisi. Suspendisse at velit urna, sed fermentum velit. Cum sociis natoque penatibus.");

		testMb0.authorNick = "author_name";
		testMb1.authorNick = "author_name";
		testMb2.authorNick = "author_name";
		testMb3.authorNick = "author_name";

		node.mbManager.getMicroblogContainer().insert(testMb0);
		node.mbManager.getMicroblogContainer().insert(testMb1);
		node.mbManager.getMicroblogContainer().insert(testMb2);
		node.mbManager.getMicroblogContainer().insert(testMb3);

	}
}
