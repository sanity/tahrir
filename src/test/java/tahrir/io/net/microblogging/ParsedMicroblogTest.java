package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.*;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.microblogs.*;
import tahrir.tools.TrUtils;

public class ParsedMicroblogTest {
	TrNode node;

	@BeforeTest
	public void setup() throws Exception {
		node = TrUtils.makeTestNode(8000, false, false, false, true, 1, 1);
	}

	@Test
	public void simpleMentionTest() throws Exception {
		final RSAPublicKey key = TrCrypto.createRsaKeyPair().a;
		final String message = "<mb><txt>Just a mention to </txt><mention>" + ParsedMicroblog.convertToMentionBytesString(key) + "</mention><txt>.</txt></mb>";
		final BroadcastMicroblog sourceMb = new BroadcastMicroblog(node, message);

		final ParsedMicroblog parsedMb = new ParsedMicroblog(sourceMb);
		Assert.assertTrue(parsedMb.hasMention(key));
	}

	@Test
	public void messagePostionTest() throws Exception {
		final RSAPublicKey key1 = TrCrypto.createRsaKeyPair().a;
		final RSAPublicKey key2 = TrCrypto.createRsaKeyPair().a;

		final String firstString = "This should be in position 0";
		final String secondString = "and this should be in position 3";

		final String message = "<mb><txt>" + firstString + "</txt><mention>" + ParsedMicroblog.convertToMentionBytesString(key1) + "</mention><mention>"
				+ ParsedMicroblog.convertToMentionBytesString(key2)
				+ "</mention><txt>" + secondString + "</txt></mb>";
		final BroadcastMicroblog sourceMb = new BroadcastMicroblog(node, message);
		final ParsedMicroblog parsedMb = new ParsedMicroblog(sourceMb);
		final Map<String, Integer> textMap = parsedMb.getText();
		final Map<RSAPublicKey, Integer> mentionsMap = parsedMb.getMentions();

		Assert.assertTrue(textMap.get(firstString).equals(0));
		Assert.assertTrue(mentionsMap.get(key1).equals(1));
		Assert.assertTrue(mentionsMap.get(key2).equals(2));
		Assert.assertTrue(textMap.get(secondString).equals(3));
	}
}
