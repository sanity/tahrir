package tahrir.network.broadcasts.broadcastMessages;

import tahrir.TrConstants;

public class BroadcastMessage {
	public int priority;
    public SignedBroadcastMessage signedBroadcastMessage;

	// for serialization
	public BroadcastMessage() {

	}

    public BroadcastMessage(final SignedBroadcastMessage signedBroadcastMessage){
        this.priority = TrConstants.BROADCAST_INIT_PRIORITY;
        this.signedBroadcastMessage = signedBroadcastMessage;
    }

	public BroadcastMessage(final SignedBroadcastMessage signedBroadcastMessage, final int priority) {
		this.priority = priority;
        this.signedBroadcastMessage = signedBroadcastMessage;
	}

    public void resetPriority(){
        this.priority = TrConstants.BROADCAST_INIT_PRIORITY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BroadcastMessage that = (BroadcastMessage) o;

        if (priority != that.priority) return false;
        if (signedBroadcastMessage != null ? !signedBroadcastMessage.equals(that.signedBroadcastMessage) : that.signedBroadcastMessage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = priority;
        result = 31 * result + (signedBroadcastMessage != null ? signedBroadcastMessage.hashCode() : 0);
        return result;
    }
}