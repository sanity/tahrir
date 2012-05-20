package tahrir.io.net.udpV1;

import java.net.InetAddress;

import tahrir.io.net.PhysicalNetworkLocation;

public class UdpNetworkLocation implements PhysicalNetworkLocation {
	public final InetAddress inetAddress;
	public final int port;

	public UdpNetworkLocation(final InetAddress inetAddress, final int port) {
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
		if (!(obj instanceof UdpNetworkLocation))
			return false;
		final UdpNetworkLocation other = (UdpNetworkLocation) obj;
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
		builder.append("UDP NetLoc[");
		builder.append(port);
		builder.append("]");
		return builder.toString();
	}

}
