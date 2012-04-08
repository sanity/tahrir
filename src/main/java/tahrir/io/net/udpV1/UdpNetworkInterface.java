package tahrir.io.net.udpV1;

import java.io.IOException;
import java.net.*;
import java.security.interfaces.*;
import java.util.Map;
import java.util.concurrent.*;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.slf4j.LoggerFactory;

import tahrir.io.net.*;
import tahrir.tools.*;

/**
 * @author Ian Clarke <ian.clarke@gmail.com>
 *
 */
public class UdpNetworkInterface extends TrNetworkInterface {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(UdpNetworkInterface.class);

	private final PriorityBlockingQueue<QueuedPacket> outbox = new PriorityBlockingQueue<UdpNetworkInterface.QueuedPacket>();
	public static final int MAX_PACKET_SIZE_BYTES = 1450;
	private final DatagramSocket datagramSocket;
	final Config config;
	private final Sender sender;

	private final Receiver receiver;

	public final RSAPrivateKey myPrivateKey;

	public Map<UdpRemoteAddress, UdpRemoteConnection> remoteConnections = Maps.newConcurrentMap();

	public final RSAPublicKey myPublicKey;

	public static class Config {
		public int listenPort;

		public volatile int maxUpstreamBytesPerSecond;
	}

	@Override
	public String toString() {
		return "UDP<" + datagramSocket.getLocalPort() + ">";
	}

	public UdpNetworkInterface(final Config config, final Tuple2<RSAPublicKey, RSAPrivateKey> keyPair)
			throws SocketException {
		this.config = config;
		myPublicKey = keyPair.a;
		myPrivateKey = keyPair.b;
		datagramSocket = new DatagramSocket(config.listenPort);
		datagramSocket.setSoTimeout(500);
		sender = new Sender(this);
		sender.start();
		receiver = new Receiver(this);
		receiver.start();
	}

	private final ConcurrentLinkedQueue<TrMessageListener> listeners = new ConcurrentLinkedQueue<TrMessageListener>();

	private final ConcurrentMap<UdpRemoteAddress, TrMessageListener> listenersByAddress = Maps
			.newConcurrentMap();

	@Override
	public void registerListener(final TrMessageListener listener) {
		listeners.add(listener);
	}

	@Override
	protected void registerListenerForSender(final TrRemoteAddress sender,
			final TrMessageListener listener) {
		if (listenersByAddress.put((UdpRemoteAddress) sender, listener) != null) {
			logger.warn("Overwriting listener for sender {}", sender);
		}
	}


	@Override
	protected void unregisterListener(
			final tahrir.io.net.TrNetworkInterface.TrMessageListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void sendTo(final TrRemoteAddress recepient_, final ByteArraySegment encryptedMessage,
			final tahrir.io.net.TrNetworkInterface.TrSentListener sentListener, final double priority) {
		final UdpRemoteAddress recepient = (UdpRemoteAddress) recepient_;
		assert encryptedMessage.length <= MAX_PACKET_SIZE_BYTES : "Packet length " + encryptedMessage.length
				+ " greater than " + MAX_PACKET_SIZE_BYTES;
		final QueuedPacket qp = new QueuedPacket(recepient, encryptedMessage, sentListener, priority);
		outbox.add(qp);
	}

	private static class Receiver extends Thread {
		public volatile boolean active = true;

		private final UdpNetworkInterface parent;

		public Receiver(final UdpNetworkInterface parent) {
			this.parent = parent;
		}

		@Override
		public void run() {

			while (active) {
				final DatagramPacket dp = new DatagramPacket(new byte[UdpNetworkInterface.MAX_PACKET_SIZE_BYTES],
						UdpNetworkInterface.MAX_PACKET_SIZE_BYTES);
				try {
					parent.datagramSocket.receive(dp);

					final UdpRemoteAddress ura = new UdpRemoteAddress(dp.getAddress(), dp.getPort());
					final tahrir.io.net.TrNetworkInterface.TrMessageListener ml = parent.listenersByAddress.get(ura);

					if (ml != null) {
						try {
							ml.received(parent, ura, ByteArraySegment.from(dp));
						} catch (final Exception e) {
							parent.logger.error(
									"Error handling received UDP packet on port "
											+ parent.datagramSocket.getLocalPort() + " from port " + dp.getPort(), e);
						}
					} else {
						for (final tahrir.io.net.TrNetworkInterface.TrMessageListener li : parent.listeners) {
							li.received(parent, ura, ByteArraySegment.from(dp));
						}
					}
				} catch (final SocketTimeoutException e) {
					// NOOP
				} catch (final IOException e) {
					parent.logger.error("Error receiving udp packet on port " + parent.datagramSocket.getLocalPort()
							+ ", receiveractive=" + active, e);
				}
			}
			parent.datagramSocket.close();
		}
	}

	private static class Sender extends Thread {
		public volatile boolean active = true;
		private final UdpNetworkInterface parent;

		public Sender(final UdpNetworkInterface parent) {
			this.parent = parent;
		}

		@Override
		public void run() {
			while (active) {
				try {
					final long startTime = System.currentTimeMillis();
					final QueuedPacket packet = parent.outbox.poll(1, TimeUnit.SECONDS);

					if (packet != null) {
						final DatagramPacket dp = new DatagramPacket(packet.data.array, packet.data.offset,
								packet.data.length,
								packet.addr.inetAddress, packet.addr.port);
						try {
							parent.datagramSocket.send(dp);
							if (packet.sentListener != null) {
								packet.sentListener.sent();
							}
						} catch (final IOException e) {
							if (packet.sentListener != null) {
								packet.sentListener.failure();
							}
							parent.logger.error("Failed to send UDP packet", e);
						}
						// System.out.println("Sent: " +
						// parent.config.listenPort + " -> " + packet.addr.port
						// + " len: "
						// + packet.data.length + " data: " + packet.data);
						Thread.sleep((1000l * packet.data.length / parent.config.maxUpstreamBytesPerSecond));
					}
				} catch (final InterruptedException e) {

				}
			}

		}
	}

	private static class QueuedPacket implements Comparable<QueuedPacket> {

		private final UdpRemoteAddress addr;
		private final ByteArraySegment data;
		private final double priority;
		private final tahrir.io.net.TrNetworkInterface.TrSentListener sentListener;

		public QueuedPacket(final UdpRemoteAddress addr, final ByteArraySegment encryptedMessage,
				final tahrir.io.net.TrNetworkInterface.TrSentListener sentListener, final double priority) {
			this.addr = addr;
			data = encryptedMessage;
			this.sentListener = sentListener;
			this.priority = priority;

		}

		public int compareTo(final QueuedPacket other) {
			return Double.compare(priority, other.priority);
		}

	}

	@Override
	public void unregisterListenerForSender(final TrRemoteAddress sender) {
		listenersByAddress.remove(sender);
	}

	@Override
	public void shutdown() {
		sender.active = false;
		sender.interrupt();
		receiver.active = false;
	}

	/**
	 * @param remoteAddress
	 * @param remotePubKey
	 * @param listener Listener for receiving inbound messages on this connection
	 * @param connectedCallback Callback informing us whether the connection was successful
	 * @param disconnectedCallback Callback for when this connection is broken
	 * @param unilateral Is the other node trying to connect back to us?
	 */
	@Override
	public TrRemoteConnection connect(final TrRemoteAddress remoteAddress,
			final RSAPublicKey remotePubKey,
			final tahrir.io.net.TrNetworkInterface.TrMessageListener listener,
			final Function<TrRemoteConnection, Void> connectedCallback,
			final Runnable disconnectedCallback, final boolean unilateral) {
		return new UdpRemoteConnection(this, (UdpRemoteAddress) remoteAddress, remotePubKey, listener,
				connectedCallback,
				disconnectedCallback, unilateral);
	}

	@Override
	protected Class<? extends TrRemoteAddress> getAddressClass() {
		return UdpRemoteAddress.class;
	}
}
