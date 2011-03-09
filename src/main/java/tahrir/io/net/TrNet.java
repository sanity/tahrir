package tahrir.io.net;


public class TrNet {
	public <T> void addMessageListener(final Class<T> c, final MessageListener<T> listener) {

	}

	public void send(final int sessionId, final Object message, final TrRemoteConnection remoteConnection) {

	}

	public Object sendAndWaitForResponse(final int sessionId, final Object message,
			final TrRemoteConnection remoteConnection) {
		return null;
	}

	public static interface MessageListener<T> {
		public AfterReceive received(T object, TrRemoteConnection source);

		public static final AfterReceive CONSUME = new AfterReceive(true, false);
		public static final AfterReceive CONSUME_AND_REMOVE = new AfterReceive(true, true);
		public static final AfterReceive CONTINUE = new AfterReceive(false, false);
		public static final AfterReceive CONTINUE_AND_REMOVE = new AfterReceive(false, true);

		public static class AfterReceive {
			public final boolean consume, remove;

			public AfterReceive(final boolean consume, final boolean remove) {
				this.consume = consume;
				this.remove = remove;
			}

		}
	}
}
