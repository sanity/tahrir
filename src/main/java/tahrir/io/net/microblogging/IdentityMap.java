package tahrir.io.net.microblogging;

import com.google.common.cache.CacheBuilder;
import tahrir.TrConstants;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * This is used for mapping nick names, public keys and shortened public keys
 * to each other.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class IdentityMap {
	private final Map<HumanReadableIdentity, RSAPublicKey> idMap;
	private final ShortenedPublicKeyFinder abbrPubKeyFinder;

	public IdentityMap(final ShortenedPublicKeyFinder abbrPublicKeyFinder, final ContactBook contactBook) {
		/*
		TODO: we could use a database here for the cache loader so that we have all identities
		ever seen recorded
		*/
		idMap = CacheBuilder.newBuilder()
				.maximumSize(TrConstants.ID_MAP_SIZE)
				.<HumanReadableIdentity, RSAPublicKey>build() // no cache loader
				.asMap();
		abbrPubKeyFinder = abbrPublicKeyFinder;
		// contacts are the people user is likely to mention first
		moveContactsIntoCache(contactBook);
	}

	public void addNewIdentity(final RSAPublicKey pubKey, final String nick) {
		final String abbrPubKey = abbrPubKeyFinder.getShortenedKey(pubKey);
		final HumanReadableIdentity readableId = new HumanReadableIdentity(nick, abbrPubKey);
		idMap.put(readableId, pubKey);
	}

	public void addNewIdentities(Map<RSAPublicKey, String> identities) {
		for (Map.Entry<RSAPublicKey, String> identity : identities.entrySet()) {
			addNewIdentity(identity.getKey(), identity.getValue());
		}
	}

	public RSAPublicKey getFullPublicKey(String alias, String abbrPubKey) {
		HumanReadableIdentity readableIdentity = new HumanReadableIdentity(alias, abbrPubKey);
		return idMap.get(readableIdentity);
	}

	private void moveContactsIntoCache(ContactBook cb) {
		for (Map.Entry<RSAPublicKey, String> entry : cb.getContacts().entrySet()) {
			addNewIdentity(entry.getKey(), entry.getValue());
		}
	}

	// dumb data object for hashing / mapping
	private static class HumanReadableIdentity {
		public String abbrPubKey;
		public String nickName;

		public HumanReadableIdentity(final String nickName, final String abbrPubKey) {
			this.nickName = nickName;
			this.abbrPubKey = abbrPubKey;
		}
	}
}
