package tahrir.network;

import java.security.interfaces.RSAPublicKey;


public class RemoteNodeAddress {
	public PhysicalNetworkLocation physicalLocation;
	public RSAPublicKey publicKey;

	/**
	 * No-arg constructor for serialization
	 */
	public RemoteNodeAddress() {

	}

	public RemoteNodeAddress(final PhysicalNetworkLocation location,final RSAPublicKey publicKey) {
		this.physicalLocation = location;
		this.publicKey = publicKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((physicalLocation == null) ? 0 : physicalLocation.hashCode());
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
		if (physicalLocation == null) {
			if (other.physicalLocation != null)
				return false;
		} else if (!physicalLocation.equals(other.physicalLocation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("RNA [loc=");
		builder.append(physicalLocation);
		builder.append("]");
		return builder.toString();
	}
}