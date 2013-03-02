package tahrir.io.net.microblogging;

import com.google.common.collect.*;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.io.crypto.TrCrypto;
import tahrir.ui.AuthorDisplayPageButton;
import tahrir.ui.TrMainWindow;

import javax.swing.*;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Comparator;
import java.util.Map;

import static tahrir.TrConstants.FormatInfo.*;

/**
 * Parser for microblogging messages.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogParser {
	private static final Logger logger = LoggerFactory.getLogger(MicroblogParser.class);

	private final SortedMultiset<ParsedPart> parsedParts = TreeMultiset.create(new PositionComparator());
	/**
	 * Records public keys mapped to aliases.
	 */
	private final Map<RSAPublicKey, String> mentionsFound = Maps.newHashMap();
	private final String messageToParse;
	private boolean parsed;

	public MicroblogParser(String messageToParse) throws ParsingException {
		this.messageToParse = messageToParse;
	}

	/**
	 * Convert parsed parts to their XML representation i.e the opposite of what we do when parsing.
	 */
	public static String getXML(SortedMultiset<ParsedPart> parsedParts) {
		Element root = new Element(ROOT);
		for (ParsedPart parsedPart : parsedParts) {
			if (parsedPart instanceof TextPart) {
				TextPart asTextPart = (TextPart) parsedPart;
				Element textElement = new Element(PLAIN_TEXT);
				textElement.appendChild(asTextPart.toText());
			} else if (parsedPart instanceof MentionPart) {
				MentionPart asMentionPart = (MentionPart) parsedPart;
				Element mentionElement = new Element(MENTION);
				// add the encoded public key
				String pubKeyAsString = TrCrypto.toBase64(asMentionPart.getPubKeyOfMentioned());
				mentionElement.appendChild(pubKeyAsString);
				// add the attribute i.e the alias
				mentionElement.addAttribute(new Attribute(ALIAS_ATTRIBUTE, asMentionPart.getAliasOfMentioned()));
			} else {
				throw new RuntimeException("Could not get XML for given multiset.");
			}
		}
		Document doc = new Document(root);
		return doc.toXML();
	}

	public ImmutableSortedMultiset<ParsedPart> getParsedParts() {
		if (!parsed) {
			throw new RuntimeException("Message hasn't been parsed.");
		}
		return ImmutableSortedMultiset.copyOfSorted(parsedParts);
	}

	public ImmutableMap<RSAPublicKey, String> getMentionsFound() {
		if (!parsed) {
			throw new RuntimeException("Message hasn't been parsed.");
		}
		return ImmutableMap.copyOf(mentionsFound);
	}

	public void parseMessage() throws ParsingException {
		final Builder builder = new Builder();
		final Document doc;
		try {
			doc = builder.build(messageToParse, null);
		} catch (IOException e) {
			throw new RuntimeException("Error getting builder for parsing");
		}
		processFromRoot(doc.getRootElement());
		parsed = true;
	}

	private void processFromRoot(final Element root) throws ParsingException {
		final Elements elements = root.getChildElements();
		for (int position = 0; position < elements.size(); position++) {
			final Element element = elements.get(position);
			if (element.getQualifiedName().equals(PLAIN_TEXT)) {
				handleText(element, position);
			} else if (element.getQualifiedName().equals(MENTION)) {
				handleMention(element, position);
			} else {
				logger.error("Unrecognised element when parsing a microblog.");
				throw new ParsingException("Unrecognised element");
			}
		}
	}

	private void handleText(Element element, int position) {
		parsedParts.add(new TextPart(position, element.getValue()));
	}

	private void handleMention(final Element element, final int position) {
		String mentionString = element.getValue();
		RSAPublicKey pubKey = TrCrypto.decodeBase64(mentionString);
		final Attribute nickNameAtt = element.getAttribute(ALIAS_ATTRIBUTE_INDEX);
		final String nickName = nickNameAtt.getValue();

		parsedParts.add(new MentionPart(position, pubKey, nickName));
		mentionsFound.put(pubKey, nickName);
	}

	/**
	 * Represents a part of a microblog that has been parsed and can now be read directly. A position is kept to record
	 * where it was found in the microblog.
	 */
	public static abstract class ParsedPart {
		private final int positionInMicroblog;

		public ParsedPart(int positionInMicroblog) {
			this.positionInMicroblog = positionInMicroblog;
		}

		public int getPositionInMicroblog() {
			return positionInMicroblog;
		}

		/**
		 * Get the textual representation of the ParesedPart. Different to a toString() in that it's intended for
		 * display to the end user.
		 *
		 * @return The textual representation.
		 */
		public abstract String toText();

		/**
		 * Get the part as a Swing component.
		 *
		 * @return The Swing part representing the part. Null if there is no Swing representation.
		 */
		public abstract JComponent toSwingComponent(TrMainWindow mainWindow);
	}

	public static class TextPart extends ParsedPart {
		private final String text;

		public TextPart(int positionInMicroblog, String text) {
			super(positionInMicroblog);
			this.text = text;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TextPart textPart = (TextPart) o;

			if (!text.equals(textPart.text)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return text.hashCode();
		}

		@Override
		public String toText() {
			return text;
		}

		@Override
		public JComponent toSwingComponent(TrMainWindow mainWindow) {
			return null;
		}
	}

	public static class MentionPart extends ParsedPart {
		private final RSAPublicKey pubKeyOfMentioned;
		private final String aliasOfMentioned;

		public MentionPart(int positionInMicroblog, RSAPublicKey pubKeyOfMentioned,
						   String aliasOfMentioned) {
			super(positionInMicroblog);
			this.pubKeyOfMentioned = pubKeyOfMentioned;
			this.aliasOfMentioned = aliasOfMentioned;
		}

		public RSAPublicKey getPubKeyOfMentioned() {
			return pubKeyOfMentioned;
		}

		public String getAliasOfMentioned() {
			return aliasOfMentioned;
		}

		@Override
		public String toText() {
			return "";
		}

		@Override
		public JComponent toSwingComponent(TrMainWindow mainWindow) {
			return new AuthorDisplayPageButton(mainWindow, pubKeyOfMentioned, aliasOfMentioned);
		}


		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			MentionPart that = (MentionPart) o;

			if (!aliasOfMentioned.equals(that.aliasOfMentioned)) return false;
			if (!pubKeyOfMentioned.equals(that.pubKeyOfMentioned)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = pubKeyOfMentioned.hashCode();
			result = 31 * result + aliasOfMentioned.hashCode();
			return result;
		}
	}

	public static class PositionComparator implements Comparator<ParsedPart> {
		@Override
		public int compare(ParsedPart o1, ParsedPart o2) {
			return Integer.compare(o1.getPositionInMicroblog(), o2.getPositionInMicroblog());
		}
	}
}
