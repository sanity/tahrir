package tahrir.io.net.microblogging.microblogs;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import nu.xom.*;

import org.slf4j.*;

import tahrir.tools.TrUtils;

import com.google.common.collect.MapMaker;

/**
 * Represents a microblog whose message (a String in XML format) has been parsed.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ParsedMicroblog {
	private static final Logger logger = LoggerFactory.getLogger(ParsedMicroblog.class);

	// Integers represent where they appeared in document
	private final Map<RSAPublicKey, Integer> mentions = new MapMaker().makeMap();
	private final Map<String, Integer> text = new MapMaker().makeMap();

	private final MicroblogForBroadcast parsedFrom;

	public ParsedMicroblog(final MicroblogForBroadcast parsedFrom) throws Exception {
		this.parsedFrom = parsedFrom;
		parseMessage();
	}

	public boolean hasMention(final RSAPublicKey userKey) {
		return mentions.containsKey(userKey);
	}

	private void parseMessage() throws Exception {
		final Builder parser = new Builder();
		final Document doc = parser.build(parsedFrom.message, null);
		final Element root = doc.getRootElement();
		processFromRoot(root);
	}

	// TODO: this is messy... maybe use command pattern?
	private void processFromRoot(final Element root) throws Exception {
		final Elements elements = root.getChildElements();
		for (int i = 0; i < elements.size(); i++) {
			final Element element = elements.get(i);
			if (element.getQualifiedName().equals("txt")) {
				handleTxtElement(element, i);
			} else if (element.getQualifiedName().equals("mention")) {
				handleMention(element, i);
			} else {
				logger.error("Unrecoginsed element");
				throw new Exception();
			}
		}
	}

	private void handleTxtElement(final Element element, final int position) {
		text.put(element.getValue(), position);
	}

	private void handleMention(final Element element, final int position) {
		final RSAPublicKey pubKey = TrUtils.getPublicKey(element.getValue());
		mentions.put(pubKey, position);
	}
}