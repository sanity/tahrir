package tahrir.io.net;


public abstract class TrNetworkInterface<RA extends TrRemoteAddress> {
	public abstract boolean canSendTo(RA remoteAddress);

	public abstract void registerListener(TrMessageListener<RA> listener);

	public abstract void unregisterListenerForSender(TrRemoteAddress sender);

	public abstract void registerListenerForSender(TrRemoteAddress sender, TrMessageListener<RA> listener);

	public abstract void unregisterListener(TrMessageListener<RA> listener);

	protected abstract void sendTo(RA recepient, byte[] message, TrSentListener sentListener, double priority);

	public void sendTo(final RA recepient, final byte[] message, final double priority) {
		sendTo(recepient, message, null, priority);
	}

	public static interface TrMessageListener<RA extends TrRemoteAddress> {
		public void received(TrNetworkInterface<RA> iFace, RA sender, byte[] message, int length);

	}

	public static interface TrSentListener {
		public void success();

		public void failure();
	}
}
