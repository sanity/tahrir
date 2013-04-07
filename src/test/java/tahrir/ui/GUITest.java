package tahrir.ui;

import java.security.interfaces.RSAPublicKey;
import java.util.SortedSet;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.MicroblogParser;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.tools.TrUtils;
import tahrir.tools.Tuple2;

import com.alee.laf.WebLookAndFeel;
import com.google.common.collect.Sets;

public class GUITest {
	public static void main(final String[] args) {
		try {
			final TrNode testNode = TrUtils.TestUtils.makeNode(9003, false, false, false, true, 0, 0);

			WebLookAndFeel.install();

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
		final Tuple2<RSAPublicKey, String> user1 = new Tuple2<RSAPublicKey, String>(TrCrypto.createRsaKeyPair().a, "user1");
		final Tuple2<RSAPublicKey, String> user2 = new Tuple2<RSAPublicKey, String>(TrCrypto.createRsaKeyPair().a, "user2");

		node.mbClasses.contactBook.addContact(user1.b, user1.a);

		final ParsedMicroblog fromRand = TrUtils.TestUtils.getParsedMicroblog();
		final ParsedMicroblog fromUser1 = TrUtils.TestUtils.getParsedMicroblog(user1);
		final ParsedMicroblog fromUser2 = TrUtils.TestUtils.getParsedMicroblog(user2, user1);
		final SortedSet<ParsedMicroblog> parsedMbs = Sets.newTreeSet(new MicroblogsForViewing.ParsedMicroblogTimeComparator());
		parsedMbs.add(fromRand);
		parsedMbs.add(fromUser1);
		parsedMbs.add(fromUser2);

		for (final ParsedMicroblog parsedMicroblog : parsedMbs) {
			final String xmlMessage = MicroblogParser.getXML(parsedMicroblog.getParsedParts());
			final BroadcastMicroblog broadcastMicroblog = new BroadcastMicroblog(xmlMessage, parsedMicroblog.getMbData());
			node.mbClasses.incomingMbHandler.handleInsertion(broadcastMicroblog);
		}
	}
}
