package tahrir.io.net.udp;

import java.io.IOException;
import java.net.*;
import java.util.Map;

import com.google.inject.internal.Maps;

public class UdpListener extends Thread {
	private final UdpReceiver defaultReceiver;
	private final DatagramSocket dsocket;
	private boolean active = true;
	private final Map<InetAddress, UdpReceiver> addressListeners = Maps.newHashMap();

	public UdpListener(final int port, final UdpReceiver defaultReceiver) throws SocketException {
		this.defaultReceiver = defaultReceiver;
		dsocket = new DatagramSocket(port);
		start();
	}

	public UdpReceiver registerListenerForAddress(final InetAddress address, final UdpReceiver receiver) {
		return addressListeners.put(address, receiver);
	}

	public void removeListenerForAddress(final InetAddress address) {
		addressListeners.remove(address);
	}

	public void close() {
		dsocket.close();
		active = false;
	}

	@Override
	public void run() {
		while (active) {
			final byte[] buffer = new byte[2048];
			final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				dsocket.receive(packet);

				final UdpReceiver udpReceiver = addressListeners.get(packet.getSocketAddress());

				if (udpReceiver != null) {
					udpReceiver.receive(packet);
				} else {
					defaultReceiver.receive(packet);
				}

			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static interface UdpReceiver {
		public void receive(DatagramPacket packet);
	}
}
