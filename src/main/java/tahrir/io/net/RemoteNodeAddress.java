package tahrir.io.net;

import java.security.interfaces.RSAPublicKey;


public class RemoteNodeAddress {
	public PhysicalNetworkLocation location;
	public RSAPublicKey publicKey;

	/**
	 * No-arg constructor for serialization
	 */
	public RemoteNodeAddress() {

	}

	public RemoteNodeAddress(final PhysicalNetworkLocation location,final RSAPublicKey publicKey) {
		this.location = location;
		this.publicKey = publicKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RemoteNodeAddress))
			return false;
		final RemoteNodeAddress other = (RemoteNodeAddress) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("RemoteNodeAddress [address=");
		builder.append(location);
		builder.append("]");
		return builder.toString();
	}
}