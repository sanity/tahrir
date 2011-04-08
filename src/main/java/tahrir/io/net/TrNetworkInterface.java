package tahrir.io.net;

import java.security.interfaces.RSAPublicKey;

import tahrir.tools.ByteArraySegment;

public abstract class TrNetworkInterface<RA extends TrRemoteAddress> {
	protected abstract void registerListener(TrMessageListener<RA> listener);

	protected abstract void unregisterListenerForSender(RA sender);

	protected abstract void registerListenerForSender(RA sender, TrMessageListener<RA> listener);

	protected abstract void unregisterListener(TrMessageListener<RA> listener);

	public abstract void shutdown();

	protected abstract void sendTo(RA recepient, ByteArraySegment message, TrSentListener sentListener, double priority);

	public void sendTo(final RA recepient, final ByteArraySegment message, final double priority) {
		sendTo(recepient, message, null, priority);
	}

	public static interface TrMessageListener<RA extends TrRemoteAddress> {
		public void received(TrNetworkInterface<RA> iFace, RA sender, ByteArraySegment message);

	}

	public abstract TrRemoteConnection<RA> getConnectionForAddress(RA address);

	public abstract TrRemoteConnection<RA> connectTo(final RA address, final RSAPublicKey remotePubkey,
			final TrMessageListener<RA> listener);

	public static final TrSentReceivedListener nullSentListener = new TrSentReceivedListener() {

		public void sent() {
		}

		public void failure() {
		}

		public void received() {
		}
	};

	public static interface TrSentListener {
		public void sent();

		public void failure();
	}

	public static interface TrSentReceivedListener extends TrSentListener {
		public void received();
	}

	public static final double CONNECTION_MAINTAINANCE_PRIORITY = 1.0;
	public static final double PACKET_RESEND_PRIORITY = 2.0;
	public static final double LONG_MESSAGE_HEADER = 3.0;
	public static final double ASSIMILATION_PRIORITY = 4.0;

	@Override
	public abstract String toString();
}
