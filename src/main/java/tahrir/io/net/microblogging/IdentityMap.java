package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import tahrir.TrConstants;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;

/**
 * This is used for mapping nick names, public keys and shortened public keys
 * to each other.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class IdentityMap {
	private final Map<RSAPublicKey, HumanReadableIdentity> idMap;
	private final ShortenedPublicKeyFinder abbrPubKeyFinder;

	public IdentityMap(final ShortenedPublicKeyFinder abbrPublicKeyFinder, final ContactBook contactBook) {
		final Cache<RSAPublicKey, HumanReadableIdentity> cache = CacheBuilder.newBuilder()
				.maximumSize(TrConstants.ID_MAP_SIZE)
				.build();
		idMap = cache.asMap();
		abbrPubKeyFinder = abbrPublicKeyFinder;
		// we will initialise with the contacts (which were persisted) because the user will probably want to
		// talk to them
	}

	public void addNewIdentity(final RSAPublicKey pubKey, final String nick) {
		final String abbrPubKey = abbrPubKeyFinder.getShortenedKey(pubKey);
		final HumanReadableIdentity readableId = new HumanReadableIdentity(nick, abbrPubKey);
		idMap.put(pubKey, readableId);
	}

	@Subscribe
	public void recordIdentityEvent(final NewIdentityEvent event) {
		addNewIdentity(event.publicKey, event.nick);
	}

	public static class NewIdentityEvent {
		public String nick;
		public RSAPublicKey publicKey;

		public NewIdentityEvent(final String nick, final RSAPublicKey publicKey) {
			this.nick = nick;
			this.publicKey = publicKey;
		}
	}

	public static class HumanReadableIdentity {
		public String abbrPubKey;
		public String nickName;

		public HumanReadableIdentity(final String nickName, final String abbrPubKey) {
			this.nickName = nickName;
			this.abbrPubKey = abbrPubKey;
		}
	}
}
