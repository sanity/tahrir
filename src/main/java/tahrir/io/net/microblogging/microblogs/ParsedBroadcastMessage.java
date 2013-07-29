package tahrir.io.net.microblogging.microblogs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Sets;
import tahrir.io.net.microblogging.BroadcastMessageParser.MentionPart;
import tahrir.io.net.microblogging.BroadcastMessageParser.ParsedPart;

import java.security.interfaces.RSAPublicKey;
import java.util.Set;

/**
 * A microblog which has been parsed.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ParsedBroadcastMessage {
	private final GeneralBroadcastMessageInfo mbData;
	private final ImmutableSortedMultiset<ParsedPart> parsedParts;
	/**
	 * Fast lookup of mentions for filtering.
	 */
	private final ImmutableSet<RSAPublicKey> mentions;


	public ParsedBroadcastMessage(final GeneralBroadcastMessageInfo mbData, ImmutableSet<RSAPublicKey> mentions,
                                  ImmutableSortedMultiset<ParsedPart> parsedParts) {
		this.mbData = mbData;
		this.mentions = mentions;
		this.parsedParts = parsedParts;
	}

	/**
	 * Easy constructor for testing purposes. Not for normal usage as it makes constructor unnecessarily slow.
	 */
	public ParsedBroadcastMessage(GeneralBroadcastMessageInfo mbData, ImmutableSortedMultiset<ParsedPart> parsedParts) {
		this.mbData = mbData;
		this.parsedParts = parsedParts;
		// extract the mentions from the parsedParts
		Set<RSAPublicKey> tempMentions = Sets.newHashSet();
		for (ParsedPart parsedPart : parsedParts) {
			if (parsedPart instanceof MentionPart) {
				MentionPart asMentionPart = (MentionPart) parsedPart;
				tempMentions.add(asMentionPart.getPubKeyOfMentioned());
			}
		}
		mentions = ImmutableSet.copyOf(tempMentions);
	}

	public boolean hasMention(final RSAPublicKey userKey) {
		return mentions.contains(userKey);
	}

	public ImmutableSortedMultiset<ParsedPart> getParsedParts() {
		return parsedParts;
	}

	public GeneralBroadcastMessageInfo getMbData() {
		return mbData;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ParsedBroadcastMessage that = (ParsedBroadcastMessage) o;

		if (!mbData.equals(that.mbData)) return false;
		if (!parsedParts.equals(that.parsedParts)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = mbData.hashCode();
		result = 31 * result + parsedParts.hashCode();
		return result;
	}
}