package tahrir.io.net.microblogging.broadcastMessages;

import com.google.common.base.Optional;
import tahrir.io.net.microblogging.UserIdentity;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Holds common state for broadcast and parsed broadcastMessages.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class GeneralBroadcastMessageInfo {
	private String languageCode;
    // TODO: Use UserIdentity here
	private String authorNick;
	private RSAPublicKey authorPubKey;
	private long timeCreated;

    // For serialization
    public GeneralBroadcastMessageInfo() {

    }

	public GeneralBroadcastMessageInfo(final String languageCode, final String authorNick,
                                       final RSAPublicKey authorPubKey, final long timeCreated) {
		this.languageCode = languageCode;
		this.authorNick = authorNick;
		this.authorPubKey = authorPubKey;
		this.timeCreated = timeCreated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorNick == null) ? 0 : authorNick.hashCode());
		result = prime * result + ((languageCode == null) ? 0 : languageCode.hashCode());
		result = prime * result + ((authorPubKey == null) ? 0 : authorPubKey.hashCode());
		result = prime * result + (int) (timeCreated ^ (timeCreated >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GeneralBroadcastMessageInfo))
			return false;
		final GeneralBroadcastMessageInfo other = (GeneralBroadcastMessageInfo) obj;
		if (authorNick == null) {
			if (other.authorNick != null)
				return false;
		} else if (!authorNick.equals(other.authorNick))
			return false;
		if (languageCode == null) {
			if (other.languageCode != null)
				return false;
		} else if (!languageCode.equals(other.languageCode))
			return false;
		if (authorPubKey == null) {
			if (other.authorPubKey != null)
				return false;
		} else if (!authorPubKey.equals(other.authorPubKey))
			return false;
		if (timeCreated != other.timeCreated)
			return false;
		return true;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public String getAuthorNick() {
		return authorNick;
	}

	public RSAPublicKey getAuthorPubKey() {
		return authorPubKey;
	}

    public UserIdentity getAuthor(){
        return new UserIdentity(authorNick, authorPubKey, Optional.<RSAPrivateKey>absent());
    }

	public long getTimeCreated() {
		return timeCreated;
	}
}