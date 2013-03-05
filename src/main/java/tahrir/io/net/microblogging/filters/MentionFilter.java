package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.security.interfaces.RSAPublicKey;
import java.util.SortedSet;

/**
 * For filtering microblogs which mentions a certain user.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MentionFilter extends MicroblogFilter {
	private final RSAPublicKey userPubKey;

	public MentionFilter(final SortedSet<ParsedMicroblog> initFrom, final RSAPublicKey userPubKey) {
		super();
		this.userPubKey = userPubKey;
		initMicroblogStorage(initFrom);
	}

	@Override
	public boolean passesFilter(final ParsedMicroblog mb) {
		return mb.hasMention(userPubKey);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userPubKey == null) ? 0 : userPubKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MentionFilter))
			return false;
		final MentionFilter other = (MentionFilter) obj;
		if (userPubKey == null) {
			if (other.userPubKey != null)
				return false;
		} else if (!userPubKey.equals(other.userPubKey))
			return false;
		return true;
	}
}
