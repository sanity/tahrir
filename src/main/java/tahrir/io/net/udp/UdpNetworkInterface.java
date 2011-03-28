package tahrir.io.net.udp;

import java.io.IOException;
import java.net.*;
import java.security.interfaces.*;
import java.util.concurrent.*;

import org.slf4j.*;

import tahrir.io.net.TrNetworkInterface;

import com.google.common.collect.Maps;

public class UdpNetworkInterface extends TrNetworkInterface<UdpRemoteAddress> {
	final Logger logger = LoggerFactory.getLogger(UdpNetworkInterface.class);

	private final PriorityBlockingQueue<QueuedPacket> outbox = new PriorityBlockingQueue<UdpNetworkInterface.QueuedPacket>();
	public static final int MAX_PACKET_SIZE_BYTES = 1450;
	private final DatagramSocket datagramSocket;
	private final Config config;
	private final Sender sender;

	private final Receiver receiver;

	public final RSAPrivateKey myPrivateKey;

	public static class Config {
		public int listenPort;

		public volatile int maxUpstreamBytesPerSecond;
	}

	public UdpNetworkInterface(final Config config, final RSAPrivateKey myPrivateKey) throws SocketException {
		this.config = config;
		this.myPrivateKey = myPrivateKey;
		datagramSocket = new DatagramSocket(config.listenPort);
		sender = new Sender(this);
		sender.start();
		receiver = new Receiver(this);
		receiver.start();
	}

	public UdpRemoteConnection connectTo(final UdpRemoteAddress address, final RSAPublicKey remotePubkey) {
		return new UdpRemoteConnection(this, address, remotePubkey);
	}

	public ConcurrentLinkedQueue<TrMessageListener<UdpRemoteAddress>> listeners = new ConcurrentLinkedQueue<TrMessageListener<UdpRemoteAddress>>();

	public ConcurrentMap<UdpRemoteAddress, TrMessageListener<UdpRemoteAddress>> listenersByAddress = Maps
	.newConcurrentMap();

	@Override
	public boolean canSendTo(final UdpRemoteAddress remoteAddress) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerListener(final TrMessageListener<UdpRemoteAddress> listener) {
		listeners.add(listener);
	}

	@Override
	public void registerListenerForSender(final UdpRemoteAddress sender,
			final TrMessageListener<UdpRemoteAddress> listener) {
		if (listenersByAddress.put(sender, listener) != null) {
			logger.warn("Overwriting listener for sender {}", sender);
		}
	}


	@Override
	public void unregisterListener(
			final tahrir.io.net.TrNetworkInterface.TrMessageListener<UdpRemoteAddress> listener) {
		listeners.remove(listener);
	}

	@Override
	protected void sendTo(final UdpRemoteAddress recepient, final byte[] message,
			final tahrir.io.net.TrNetworkInterface.TrSentListener sentListener, final double priority) {
		assert message.length <= MAX_PACKET_SIZE_BYTES;
		final QueuedPacket qp = new QueuedPacket(recepient, message, sentListener, priority);
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

					final tahrir.io.net.TrNetworkInterface.TrMessageListener<UdpRemoteAddress> ml = parent.listenersByAddress.get(ura);

					if (ml != null) {
						try {
							ml.received(parent, ura, dp.getData(), dp.getLength());
						} catch (final Exception e) {
							parent.logger.error("Error handling received UDP packet", e);
						}
					} else {
						for (final tahrir.io.net.TrNetworkInterface.TrMessageListener<UdpRemoteAddress> li : parent.listeners) {
							li.received(parent, ura, dp.getData(), dp.getLength());
						}
					}

				} catch (final IOException e) {
					parent.logger.error("Error receiving udp packet", e);
				}
			}
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
						final DatagramPacket dp = new DatagramPacket(packet.data, packet.data.length,
								packet.addr.inetAddress, packet.addr.port);
						try {
							parent.datagramSocket.send(dp);
							if (packet.sentListener != null) {
								packet.sentListener.success();
							}
						} catch (final IOException e) {
							if (packet.sentListener != null) {
								packet.sentListener.failure();
							}
							parent.logger.error("Failed to send UDP packet", e);
						}
						Thread.sleep((1000l * packet.data.length / parent.config.maxUpstreamBytesPerSecond));
					}
				} catch (final InterruptedException e) {

				}
			}

		}
	}

	private static class QueuedPacket implements Comparable<QueuedPacket> {

		private final UdpRemoteAddress addr;
		private final byte[] data;
		private final double priority;
		private final tahrir.io.net.TrNetworkInterface.TrSentListener sentListener;

		public QueuedPacket(final UdpRemoteAddress addr, final byte[] data,
				final tahrir.io.net.TrNetworkInterface.TrSentListener sentListener, final double priority) {
			this.addr = addr;
			this.data = data;
			this.sentListener = sentListener;
			this.priority = priority;

		}

		public int compareTo(final QueuedPacket other) {
			return Double.compare(priority, other.priority);
		}

	}

	@Override
	public void unregisterListenerForSender(final UdpRemoteAddress sender) {
		listenersByAddress.remove(sender);
	}

	@Override
	public void shutdown() {
		sender.active = false;
		receiver.active = false;
		datagramSocket.close();
	}
}
