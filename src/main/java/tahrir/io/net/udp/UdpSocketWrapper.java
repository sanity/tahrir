package tahrir.io.net.udp;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpSocketWrapper extends Thread {
	private final DatagramSocket dsocket;
	private boolean active = true;
	private final ConcurrentLinkedQueue<UdpReceiver> addressListeners = new ConcurrentLinkedQueue<UdpReceiver>();

	public UdpSocketWrapper(final int port) throws SocketException {
		dsocket = new DatagramSocket(port);
		start();
	}

	public void registerListener(final UdpReceiver receiver) {
		addressListeners.add(receiver);
	}

	public void send(final DatagramPacket toSend) throws IOException {
		dsocket.send(toSend);
	}

	public void removeListener(final UdpReceiver listener) {
		addressListeners.remove(listener);
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
				for (final UdpReceiver al : addressListeners) {
					if (al.receive(packet)) {
						break;
					}
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static interface UdpReceiver {
		/**
		 * 
		 * @param packet
		 * @return True if this receiver "consumes" the packet
		 */
		public boolean receive(DatagramPacket packet);
	}
}
