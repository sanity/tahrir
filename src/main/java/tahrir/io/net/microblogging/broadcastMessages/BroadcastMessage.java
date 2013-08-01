package tahrir.io.net.microblogging.broadcastMessages;

public class BroadcastMessage {
	public int priority;
    private SignedBroadcastMessage broadcastMessage;

	// for serialization
	public BroadcastMessage() {

	}

	public BroadcastMessage(final SignedBroadcastMessage broadcastMessage, final int priority) {
		this.priority = priority;
        this.broadcastMessage = broadcastMessage;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BroadcastMessage that = (BroadcastMessage) o;

        if (priority != that.priority) return false;
        if (broadcastMessage != null ? !broadcastMessage.equals(that.broadcastMessage) : that.broadcastMessage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = priority;
        result = 31 * result + (broadcastMessage != null ? broadcastMessage.hashCode() : 0);
        return result;
    }
}