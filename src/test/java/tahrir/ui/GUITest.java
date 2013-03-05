package tahrir.ui;

import com.google.common.collect.Sets;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.MicroblogParser;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.tools.TrUtils;
import tahrir.tools.Tuple2;

import java.security.interfaces.RSAPublicKey;
import java.util.SortedSet;

public class GUITest {
	public static void main(final String[] args) {
		try {
			final TrNode testNode = TrUtils.TestUtils.makeNode(9003, false, false, false, true, 0, 0);

			//UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

			final TrMainWindow mainWindow = new TrMainWindow(testNode);
			mainWindow.getContent().revalidate();
			GUITest.addTestInformationToNode(testNode);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void addTestInformationToNode(final TrNode node) {
		/*
		  This is pretty silly: creating parsed microblogs and then, using their information, turn them into
		  their unparsed form and later insert them as if they were from broadcast.
		 */
		Tuple2<RSAPublicKey, String> user1 = new Tuple2<RSAPublicKey, String>(TrCrypto.createRsaKeyPair().a, "user1");
		Tuple2<RSAPublicKey, String> user2 = new Tuple2<RSAPublicKey, String>(TrCrypto.createRsaKeyPair().a, "user2");

		node.mbClasses.contactBook.addContact(user1.b, user1.a);

		ParsedMicroblog fromRand = TrUtils.TestUtils.getParsedMicroblog();
		ParsedMicroblog fromUser1 = TrUtils.TestUtils.getParsedMicroblog(user1);
		ParsedMicroblog fromUser2 = TrUtils.TestUtils.getParsedMicroblog(user2, user1);
		SortedSet<ParsedMicroblog> parsedMbs = Sets.newTreeSet(new MicroblogsForViewing.ParsedMicroblogTimeComparator());
		parsedMbs.add(fromRand);
		parsedMbs.add(fromUser1);
		parsedMbs.add(fromUser2);

		for (ParsedMicroblog parsedMicroblog : parsedMbs) {
			String xmlMessage = MicroblogParser.getXML(parsedMicroblog.getParsedParts());
			BroadcastMicroblog broadcastMicroblog = new BroadcastMicroblog(xmlMessage, parsedMicroblog.getMbData());
			node.mbClasses.incomingMbHandler.handleInsertion(broadcastMicroblog);
		}
	}
}
