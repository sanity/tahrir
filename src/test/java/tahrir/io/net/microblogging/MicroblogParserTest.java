package tahrir.io.net.microblogging;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tahrir.TrConstants.FormatInfo.PLAIN_TEXT;
import static tahrir.TrConstants.FormatInfo.ROOT;

public class MicroblogParserTest {
	@Test
	public void simpleTextTest() {
		// create a simple Tahrir xml document with nothing other than text
		Element root = new Element(ROOT);
		Element textElement = new Element(PLAIN_TEXT);
		root.appendChild(textElement);
		String text = "Hello, world! This is a very simple microblog!";
		textElement.appendChild(text);
		Document doc = new Document(root);
		String xml = doc.toXML();

		MicroblogParser parser = null;
		try {
			parser = new MicroblogParser(xml);
		} catch (ParsingException e) {
			throw new RuntimeException("Couldn't parse the XML.", e);
		}
		// convert back to XML and compare with original
		Assert.assertEquals(MicroblogParser.getXML(parser.getParsedParts()), xml);
		Assert.assertTrue(parser.getMentionsFound().size() == 0);
	}

	@Test
	public void withMentionsTest() throws Exception {

	}
}
