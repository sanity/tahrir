package tahrir.io.net;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.tools.ByteArraySegment;

import com.google.common.base.Function;

public abstract class TrRemoteConnection<RA extends TrRemoteAddress> {
	protected final TrMessageListener<RA> listener;
	protected final Function<TrRemoteConnection<RA>, Void> connectedCallback;
	protected final RA remoteAddress;
	protected RSAPublicKey remotePubKey;
	protected final Runnable disconnectedCallback;
	protected final boolean unilateral;

	protected TrRemoteConnection(final RA remoteAddress, final RSAPublicKey remotePubKey,
			final TrMessageListener<RA> listener,
			final Function<TrRemoteConnection<RA>, Void> connectedCallback,
			final Runnable disconnectedCallback, final boolean unilateral) {
		this.remoteAddress = remoteAddress;
		this.remotePubKey = remotePubKey;
		this.listener = listener;
		this.connectedCallback = connectedCallback;
		this.disconnectedCallback = disconnectedCallback;
		this.unilateral = unilateral;
	}

	public abstract void send(final ByteArraySegment message, final double priority,
			final TrSentReceivedListener sentListener) throws IOException;

	public abstract boolean isConnected();

	public boolean wasInboundUnilateral() {
		return remotePubKey == null;
	}

	public boolean wasOutboundUnilateral() {
		return unilateral;
	}

	public abstract void disconnect();
}
