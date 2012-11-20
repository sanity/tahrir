package tahrir.io.net.microblogging.microblogs;

import java.security.interfaces.RSAPublicKey;

import tahrir.*;
import tahrir.io.crypto.*;

public class Microblog {
	public int priority;
	public TrSignature signature;
	public String languageCode;
	public String authorNick;
	public RSAPublicKey publicKey;
	public String message;
	public long timeCreated;

	// for serialization
	public Microblog() {

	}

	public Microblog(final TrNode creatingNode, final String message) {
		this(creatingNode, message, TrConstants.BROADCAST_INIT_PRIORITY);
	}

	public Microblog(final TrNode creatingNode, final String message, final int priority) {
		this.priority = priority;
		timeCreated = System.currentTimeMillis();
		this.message = message;
		languageCode = ""; // TODO: get language code from config?
		authorNick = ""; // TODO: get nick from config?
		publicKey = creatingNode.getRemoteNodeAddress().publicKey;
		try {
			signature = TrCrypto.sign(message, creatingNode.getPrivateNodeId().privateKey);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Microblog(final int priority, final String authorNick, final RSAPublicKey publicKey, final String message, final long timeCreated) {
		this.priority = priority;
		this.authorNick = authorNick;
		this.publicKey = publicKey;
		this.message = message;
		this.timeCreated = timeCreated;
		try {
			signature = TrCrypto.sign(message, TrCrypto.createRsaKeyPair().b);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
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
		final Microblog other = (Microblog) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}
}