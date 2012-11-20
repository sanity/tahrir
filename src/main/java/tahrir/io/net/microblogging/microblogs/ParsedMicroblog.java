package tahrir.io.net.microblogging.microblogs;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import nu.xom.*;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.*;

import tahrir.tools.TrUtils;

import com.google.common.collect.MapMaker;

/**
 * Represents a microblog whose message (a String in XML format) has been parsed.
 * 
 * TODO: move all the parsing stuff to a separate class.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ParsedMicroblog {
	private static final Logger logger = LoggerFactory.getLogger(ParsedMicroblog.class);

	public final Microblog sourceMb;

	// Integers represent where they appeared in document
	private final Map<RSAPublicKey, Integer> mentions = new MapMaker().makeMap();
	private final Map<String, Integer> text = new MapMaker().makeMap();

	public ParsedMicroblog(final Microblog parsedFrom) throws Exception {
		sourceMb = parsedFrom;
		parseMessage();
	}

	public boolean hasMention(final RSAPublicKey userKey) {
		return mentions.containsKey(userKey);
	}

	public Map<RSAPublicKey, Integer> getMentions() {
		return mentions;
	}

	public Map<String, Integer> getText() {
		return text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceMb == null) ? 0 : sourceMb.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ParsedMicroblog other = (ParsedMicroblog) obj;
		if (sourceMb == null) {
			if (other.sourceMb != null)
				return false;
		} else if (!sourceMb.equals(other.sourceMb))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParsedMicroblog [text=" + text + "]";
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

	private void parseMessage() throws Exception {
		final Builder parser = new Builder();
		final Document doc = parser.build(sourceMb.message, null);
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
		// turn from string into individual bytes
		final String bytesString = element.getValue();
		final String[] bytesStringArray = bytesString.split(" ");
		final Byte[] bytes = new Byte[bytesStringArray.length];
		for (int i = 0; i < bytesStringArray.length; i++) {
			bytes[i] = Byte.parseByte(bytesStringArray[i]);
		}

		final RSAPublicKey pubKey = TrUtils.getPublicKey(ArrayUtils.toPrimitive(bytes));
		mentions.put(pubKey, position);
	}
}