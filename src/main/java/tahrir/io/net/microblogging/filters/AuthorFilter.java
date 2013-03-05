package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.security.interfaces.RSAPublicKey;
import java.util.SortedSet;

/**
 * For filtering microblogs by a particular author.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class AuthorFilter extends MicroblogFilter {
	private final RSAPublicKey authorsKey;

	public AuthorFilter(final SortedSet<ParsedMicroblog> initFrom, final RSAPublicKey authorsKey) {
		super();
		this.authorsKey = authorsKey;
		initMicroblogStorage(initFrom);
	}

	@Override
	public boolean passesFilter(final ParsedMicroblog mb) {
		return mb.getMbData().getAuthorPubKey().equals(authorsKey);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((authorsKey == null) ? 0 : authorsKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AuthorFilter))
			return false;
		final AuthorFilter other = (AuthorFilter) obj;
		if (authorsKey == null) {
			if (other.authorsKey != null)
				return false;
		} else if (!authorsKey.equals(other.authorsKey))
			return false;
		return true;
	}
}
