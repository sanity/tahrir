package tahrir.io.net.microblogging.microblogs;

import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.crypto.TrSignature;

/**
 * A microblog for broadcast.
 * 
 * The message is in a XML format.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class BroadcastMicroblog {
	public int priority;
	public GeneralMicroblogInfo otherData;
	public String message;
	public TrSignature signature;

	// for serialization
	public BroadcastMicroblog() {

	}

	public BroadcastMicroblog(final TrNode creatingNode, final String message) {
		this(creatingNode, message, TrConstants.BROADCAST_INIT_PRIORITY);
	}

	public BroadcastMicroblog(final TrNode creatingNode, final String message, final int priority) {
		// TODO: get info from config
		otherData = new GeneralMicroblogInfo("", "", creatingNode.getRemoteNodeAddress().publicKey, System.currentTimeMillis());
		this.priority = priority;
		this.message = message;
		otherData.authorPubKey = creatingNode.getRemoteNodeAddress().publicKey;
		try {
			signature = TrCrypto.sign(message, creatingNode.getPrivateNodeId().privateKey);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * For tests.
	 * @param message
	 * @param otherData
	 */
	public BroadcastMicroblog(String message, GeneralMicroblogInfo otherData) {
		this.message = message;
		this.otherData = otherData;
		this.signature = null;
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
		final BroadcastMicroblog other = (BroadcastMicroblog) obj;
		if (signature == null) {
			if (signature != null)
				return false;
		} else if (!signature.equals(signature))
			return false;
		return true;
	}
}