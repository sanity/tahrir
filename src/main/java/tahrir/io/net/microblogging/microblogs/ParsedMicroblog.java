package tahrir.io.net.microblogging.microblogs;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tahrir.io.net.microblogging.FormatInfo;
import tahrir.io.net.microblogging.IdentityMap.NewIdentityEvent;
import tahrir.tools.TrUtils;

import com.google.common.collect.MapMaker;

/**
 * A microblog which has been created from parsing a XML based message.
 * 
 * Moving all the parsing stuff to a separate class would probably be a good idea
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ParsedMicroblog {
	private static final Logger logger = LoggerFactory.getLogger(ParsedMicroblog.class);

	public GeneralMicroblogInfo mbData;

	// Integers represent where they appeared in message
	private final Map<RSAPublicKey, Integer> mentions = new MapMaker().makeMap();
	private final Map<String, Integer> text = new MapMaker().makeMap();

	public ParsedMicroblog(final GeneralMicroblogInfo mbData, final String unparsedMessage) throws Exception {
		this.mbData = mbData;
		parseMessage(unparsedMessage);
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

	public int getElementCount() {
		return mentions.size() + text.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mbData == null) ? 0 : mbData.hashCode());
		result = prime * result + ((mentions == null) ? 0 : mentions.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ParsedMicroblog))
			return false;
		final ParsedMicroblog other = (ParsedMicroblog) obj;
		if (mbData == null) {
			if (other.mbData != null)
				return false;
		} else if (!mbData.equals(other.mbData))
			return false;
		if (mentions == null) {
			if (other.mentions != null)
				return false;
		} else if (!mentions.equals(other.mentions))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
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

	private void parseMessage(final String unparsedMessage) throws Exception {
		final Builder parser = new Builder();
		final Document doc = parser.build(unparsedMessage, null);
		final Element root = doc.getRootElement();
		processFromRoot(root);
	}

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

		final Attribute nickNameAtt = element.getAttribute(FormatInfo.NICK_NAME_ATTRIBUTE_INDEX);
		final String nickName = nickNameAtt.getValue();
		// notify the identity with the new identity discovered from parsing
		TrUtils.eventBus.post(new NewIdentityEvent(nickName, pubKey));
	}
}