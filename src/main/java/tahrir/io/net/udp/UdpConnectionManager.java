package tahrir.io.net.udp;

import java.net.InetAddress;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import tahrir.io.net.TrRemoteConnection;

public class UdpConnectionManager extends Thread {

	protected final ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(2);

	private final UdpListener udpListener;

	public UdpConnectionManager(final UdpListener udpListener) {
		this.udpListener = udpListener;
	}

	public void close() {
		udpListener.close();
	}

	public Set<UdpConnection> getActiveConnections() {
		return null;
	}

	public UdpConnection establishConnection(final RSAPublicKey connPubkey, final InetAddress address,
			final TrRemoteConnection.StateChangeListener connectedListener) {
		final UdpConnection connection = new UdpConnection(this, connPubkey, address);
		connection.registerStateChangeListener(TrRemoteConnection.ConnState.ACTIVE, connectedListener);

		return connection;
	}
}
