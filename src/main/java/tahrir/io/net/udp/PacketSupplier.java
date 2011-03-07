package tahrir.io.net.udp;

import java.net.DatagramPacket;

import tahrir.tools.Tuple2;

public abstract class PacketSupplier implements Comparable<PacketSupplier> {
	private final double priority;

	public PacketSupplier(final double priority) {
		this.priority = priority;

	}

	public abstract Tuple2<DatagramPacket, Boolean> getPacket();

	public int compareTo(final PacketSupplier o) {
		if (priority > o.priority)
			return 1;
		else if (priority < o.priority)
			return -1;
		else
			return 0;
	}
}