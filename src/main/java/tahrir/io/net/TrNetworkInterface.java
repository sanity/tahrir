package tahrir.io.net;


public abstract class TrNetworkInterface<RA extends TrRemoteAddress> {
	public abstract boolean canSendTo(RA remoteAddress);

	public abstract void registerListener(TrMessageListener<RA> listener);

	public abstract void unregisterListenerForSender(RA sender);

	public abstract void registerListenerForSender(RA sender, TrMessageListener<RA> listener);

	public abstract void unregisterListener(TrMessageListener<RA> listener);

	public abstract void shutdown();

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

	public static final double CONNECTION_MAINTAINANCE_PRIORITY = 1.0;
	public static final double PACKET_RESEND_PRIORITY = 2.0;
}
