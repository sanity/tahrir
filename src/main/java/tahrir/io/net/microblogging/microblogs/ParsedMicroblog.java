package tahrir.io.net.microblogging.microblogs;

import com.google.common.collect.ImmutableMap;
import tahrir.tools.Tuple2;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * A microblog which has been parsed.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ParsedMicroblog {
	public GeneralMicroblogInfo mbData;

	// Integers represent where they appeared in message
	private ImmutableMap<Tuple2<RSAPublicKey, String>, Integer> mentions;
	private ImmutableMap<String, Integer> text;

	public ParsedMicroblog(final GeneralMicroblogInfo mbData,
				ImmutableMap<Tuple2<RSAPublicKey, String>, Integer> mentions,
				ImmutableMap<String, Integer> text) {
		this.mbData = mbData;
		this.mentions = mentions;
		this.text = text;
	}

	public boolean hasMention(final RSAPublicKey userKey) {
		return mentions.containsKey(userKey);
	}

	public Map<Tuple2<RSAPublicKey, String>, Integer> getMentions() {
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
}