package tahrir.io.net.microblogging.microblogs;

import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.crypto.TrSignature;

/**
 * A microblog for broadcast.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class BroadcastMessage {
	public int priority;
	public GeneralBroadcastMessageInfo otherData;
	/**
	 * Message is the microblog in an XML format.
	 */
	public String message;
	public TrSignature signature;

	// for serialization
	public BroadcastMessage() {

	}

	public BroadcastMessage(final TrNode creatingNode, final String message) {
		this(creatingNode, message, TrConstants.BROADCAST_INIT_PRIORITY);
	}

	public BroadcastMessage(final TrNode creatingNode, final String message, final int priority) {
		// TODO: get info from config
		otherData = new GeneralBroadcastMessageInfo("", creatingNode.config.currentUserIdentity.getNick(), creatingNode.getRemoteNodeAddress().publicKey, System.currentTimeMillis());
		this.priority = priority;
		this.message = message;
		try {
			signature = TrCrypto.sign(message, creatingNode.getPrivateNodeId().privateKey);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * For tests.
	 *
	 * @param message
	 * @param otherData
	 */
	public BroadcastMessage(String message, GeneralBroadcastMessageInfo otherData) {
		this.message = message;
		this.otherData = otherData;
		this.signature = null;
	}

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BroadcastMessage broadcastMessage = (BroadcastMessage) o;

        if (priority != broadcastMessage.priority) return false;
        if (!message.equals(broadcastMessage.message)) return false;
        if (!otherData.equals(broadcastMessage.otherData)) return false;
        if(signature != null && broadcastMessage.signature != null) {
        if (!signature.equals(broadcastMessage.signature)) return false;
        }
        if(signature == null && broadcastMessage.signature == null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = priority;
        result = 31 * result + otherData.hashCode();
        result = 31 * result + message.hashCode();
        if(signature != null){
        result = 31 * result + signature.hashCode();
        }
        else{
            result = 31 * result;
        }
        return result;
    }
}