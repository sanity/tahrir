package tahrir.io.net.microblogging.filters;

import java.security.interfaces.RSAPublicKey;

import tahrir.io.net.microblogging.Microblog;

public class UserFilter implements MicroblogFilter {
	private final RSAPublicKey userPubKey;

	public UserFilter(final RSAPublicKey userPubKey) {
		this.userPubKey = userPubKey;
	}

	@Override
	public boolean passesFilter(final Microblog mb) {
		if (mb.publicKey.equals(userPubKey))
			return true;
		else
			return false;
	}
}
