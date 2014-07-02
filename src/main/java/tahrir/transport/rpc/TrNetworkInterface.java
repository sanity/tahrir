package tahrir.transport.rpc;

import java.security.interfaces.RSAPublicKey;

import com.google.common.base.Function;

import tahrir.network.PhysicalNetworkLocation;
import tahrir.util.tools.ByteArraySegment;

public abstract class TrNetworkInterface {
	public static final double ASSIMILATION_PRIORITY = 4.0;

	public static final double CONNECTION_MAINTAINANCE_PRIORITY = 1.0;

	public static final double LONG_MESSAGE_HEADER = 3.0;

	public static final TrSentReceivedListener nullSentListener = new TrSentReceivedListener() {

		public void failure() {
		}

		public void received() {
		}

		public void sent() {
		}
	};

	public static final double PACKET_RESEND_PRIORITY = 2.0;

	protected volatile TrMessageListener newConnectionListener = null;

	public abstract TrRemoteConnection connect(final PhysicalNetworkLocation remoteAddress, final RSAPublicKey remotePubKey,
			final TrMessageListener listener, final Function<TrRemoteConnection, Void> connectedCallback,
			final Runnable disconnectedCallback, boolean unilateral);

	public abstract void shutdown();

	@Override
	public abstract String toString();

	protected abstract Class<? extends PhysicalNetworkLocation> getAddressClass();

	public void sendTo(final PhysicalNetworkLocation recepient, final ByteArraySegment message, final double priority) {
		sendTo(recepient, message, null, priority);
	}
	protected abstract void sendTo(PhysicalNetworkLocation recepient, ByteArraySegment message, TrSentListener sentListener,
			double priority);
	public void allowUnsolicitedInbound(final TrMessageListener newConnectionListener) {
		if (this.newConnectionListener != null)
			throw new RuntimeException("Only one newConnectionListener can be registered at a time");
		this.newConnectionListener = newConnectionListener;
	}

	public void disallowUnsolicitedInbound() {
		newConnectionListener = null;
	}
	public static interface TrMessageListener {
		public void received(TrNetworkInterface iFace, PhysicalNetworkLocation sender, ByteArraySegment message);

	}
	public static interface TrSentListener {
		public void failure();

		public void sent();
	}

	public static interface TrSentReceivedListener extends TrSentListener {
		public void received();
	}
}
