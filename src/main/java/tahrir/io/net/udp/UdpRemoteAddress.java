package tahrir.io.net.udp;

import java.net.InetAddress;

import tahrir.io.net.TrRemoteAddress;

public class UdpRemoteAddress implements TrRemoteAddress {
	public final InetAddress inetAddress;
	public final int port;

	public UdpRemoteAddress(final InetAddress inetAddress, final int port) {
		this.inetAddress = inetAddress;
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inetAddress == null) ? 0 : inetAddress.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UdpRemoteAddress))
			return false;
		final UdpRemoteAddress other = (UdpRemoteAddress) obj;
		if (inetAddress == null) {
			if (other.inetAddress != null)
				return false;
		} else if (!inetAddress.equals(other.inetAddress))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("UdpRemoteAddress [inetAddress=");
		builder.append(inetAddress);
		builder.append(", port=");
		builder.append(port);
		builder.append("]");
		return builder.toString();
	}

}
