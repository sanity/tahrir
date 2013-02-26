package tahrir.io.net.microblogging;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.io.crypto.TrCrypto;
import tahrir.tools.Tuple2;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import static tahrir.TrConstants.FormatInfo.*;

/**
 * Parser for microblogging messages.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogParser {
	private static final Logger logger = LoggerFactory.getLogger(MicroblogParser.class);

	/**
	 * Records mentions and text discovered while parsing. Integer represents relative location in the message.
	 */
	private Map<Tuple2<RSAPublicKey, String>, Integer> mentions = Maps.newHashMap();
	private Map<String, Integer> text = Maps.newHashMap();
	/**
	 * Records identities discovered while parsing.
	 */
	private Map<RSAPublicKey, String> identitiesDiscovered = Maps.newHashMap();

	private String messageToParse;
	private boolean parsed;

	public MicroblogParser(String messageToParse) throws ParsingException {
		this.messageToParse = messageToParse;
	}

	public ImmutableMap<Tuple2<RSAPublicKey, String>, Integer> getMentions() {
		if (!parsed) {
			logger.error("The message wasn't parsed yet.");
			return null;
		}
		return ImmutableMap.copyOf(mentions);
	}

	public ImmutableMap<String, Integer> getText() {
		if (!parsed) {
			logger.error("The message wasn't parsed yet.");
			return null;
		}
		return ImmutableMap.copyOf(text);
	}

	public ImmutableMap<RSAPublicKey, String> getIdentitiesDiscovered() {
		if (!parsed) {
			logger.error("The message wasn't parsed yet.");
			return null;
		}
		return ImmutableMap.copyOf(identitiesDiscovered);
	}

	/**
	 * A method for converting a public key into a String which represents a mention
	 * for the user with the particular public key.
	 * @param pubKey The public key of the user who will be mentioned
	 * @return The public key encoded as bytes ready for markup.
	 */
	public static String convertToMentionBytesString(final RSAPublicKey pubKey) {
		final StringBuilder builder = new StringBuilder();
		for (final Byte encodedByte: pubKey.getEncoded()) {
			builder.append(encodedByte);
			builder.append(" ");
		}

		return builder.toString();
	}

	public void parseMessage() throws ParsingException {
		final Builder builder = new Builder();
		final Document doc;
		try {
			doc = builder.build(messageToParse, null);
		} catch (IOException e) {
			logger.error("Error getting builder for parsing");
			throw new RuntimeException(e);
		}
		final Element root = doc.getRootElement();
		processFromRoot(root);
		parsed = true;
	}

	/**
	 * Utility method for converting maps to Tahrir formatted XML.
	 *
	 * @param mentions A for mention -> location in document
	 * @param text A map for plain text -> location in document
	 * @return The XML as a String.
	 */
	public static String getXML(Map<Tuple2<RSAPublicKey, String>, Integer> mentions, Map<String, Integer> text) {
		Element[] elements = new Element[mentions.size() + text.size()];
		for (Map.Entry<Tuple2<RSAPublicKey, String>, Integer> mentionEntry : mentions.entrySet()) {
			Integer docPosition = mentionEntry.getValue();

			Element mentionElement = new Element(MENTION);
			String pubKeyAsString = TrCrypto.toBase64(mentionEntry.getKey().a);
			mentionElement.appendChild(pubKeyAsString);

			Attribute aliasAttribute = new Attribute(ALIAS_ATTRIBUTE, mentionEntry.getKey().b);
			mentionElement.addAttribute(aliasAttribute);

			elements[docPosition] = mentionElement;
		}
		for (Map.Entry<String, Integer> textEntry : text.entrySet()) {
			Element textElement = new Element(PLAIN_TEXT);
			textElement.appendChild(textEntry.getKey());
		}
		Element root = new Element(ROOT);
		for (Element e : elements) {
			root.appendChild(e);
		}
		Document doc = new Document(root);
		return doc.toXML();
	}

	private void processFromRoot(final Element root) throws ParsingException {
		final Elements elements = root.getChildElements();
		for (int position = 0; position < elements.size(); position++) {
			final Element element = elements.get(position);
			if (element.getQualifiedName().equals(PLAIN_TEXT)) {
				// plain text element
				text.put(element.getValue(), position);
			} else if (element.getQualifiedName().equals(MENTION)) {
				// mention element
				handleMention(element, position);
			} else {
				logger.error("Unrecognised element when parsing a microblog.");
				throw new ParsingException("Unrecognised element");
			}
		}
	}

	private void handleMention(final Element element, final int position) {
		String mentionString = element.getValue();
		RSAPublicKey pubKey = TrCrypto.decodeBase64(mentionString);
		final Attribute nickNameAtt = element.getAttribute(ALIAS_ATTRIBUTE_INDEX);
		final String nickName = nickNameAtt.getValue();

		mentions.put(new Tuple2<RSAPublicKey, String>(pubKey, nickName), position);
		identitiesDiscovered.put(pubKey, nickName);
	}
}
