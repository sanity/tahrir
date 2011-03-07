package tahrir.io.net.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.concurrent.*;

import tahrir.io.net.TrRemoteConnection;

public class UdpConnectionManager extends Thread {

	protected final ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(2);

	protected final PriorityBlockingQueue<PrioritizedUdpPacket> sendQueue = new PriorityBlockingQueue<PrioritizedUdpPacket>();

	private final UdpSocketWrapper udp;

	private boolean active = true;

	public UdpConnectionManager(final UdpSocketWrapper udp) {
		this.udp = udp;
		start();
	}

	@Override
	public void run() {
		while (active) {
			try {
				final PrioritizedUdpPacket packet = sendQueue.poll(1, TimeUnit.SECONDS);
				if (packet != null) {
					try {
						udp.send(packet.packet);
						if (packet.sentListener != null) {
							packet.sentListener.sent();
						}
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}
			} catch (final InterruptedException e) {

			}
		}
	}

	public void close() {
		udp.close();
		active = false;
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
