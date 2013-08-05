package tahrir.io.net.broadcasts;

import nu.xom.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.tools.TrUtils;

import static tahrir.TrConstants.FormatInfo.*;

public class BroadcastMessageParserTest {
	/**
	 * Create a simple micoblog with nothing but text.
	 */



    @Test
	public void simpleTextTest() throws Exception {

            TrNode node = TrUtils.TestUtils.makeNode(9003, false, false, false, true, 0, 0);

		String xml = "<bm><txt>Hello there this is simple test.</txt></bm>" ;
        ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("Hello there this is simple test.", "en", node.mbClasses.identityStore);

		// convert back to XML and compare with original
		Assert.assertTrue(parsedBroadcastMessage.asXmlString().equals(xml));
	}

	/**
	 * Create a microblog with both text and mentions.
	 */
	/*@Test
	public void withMentionsTest() throws Exception {
		Element root = getRootWithText();
		Element mentionElement = new Element(MENTION);
		root.appendChild(mentionElement);

		// Append a mention. In accordance with FormatInfo then the mention is encoded an a String in base64.
		mentionElement.addAttribute(new Attribute(ALIAS_ATTRIBUTE, "user1337"));
		String base64Mention = TrCrypto.toBase64(TrCrypto.createRsaKeyPair().a);
		mentionElement.appendChild(base64Mention);

		String xml = new Document(root).toXML();
		BroadcastMessageParser parser = new BroadcastMessageParser(xml);

		// convert back to XML and compare with original
		Assert.assertEquals(BroadcastMessageParser.getXML(parser.getParsedParts()), xml);

		Assert.assertTrue(parser.getMentionsFound().size() == 1);
	} */

	/**
	 * Get a root element with an attached text element.
	 */
	private Element getRootWithText() {
		Element root = new Element(ROOT);
		Element textElement = new Element(PLAIN_TEXT);
		root.appendChild(textElement);

		String text = "Hello, world! This is a very simple microblog!";
		textElement.appendChild(text);

		return root;
	}
}
