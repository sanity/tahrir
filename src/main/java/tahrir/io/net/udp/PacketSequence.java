package tahrir.io.net.udp;

import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentLinkedQueue;

import tahrir.tools.Tuple2;

public class PacketSequence extends PacketSupplier {
	private final ConcurrentLinkedQueue<DatagramPacket> packets = new ConcurrentLinkedQueue<DatagramPacket>();

	public PacketSequence(final double priority, final DatagramPacket... packets) {
		super(priority);
		for (final DatagramPacket dp : packets) {
			this.packets.add(dp);
		}
	}

	@Override
	public Tuple2<DatagramPacket, Boolean> getPacket() {
		final DatagramPacket ret = packets.poll();
		return Tuple2.of(ret, !packets.isEmpty());
	}


}
