package tahrir.transport.messaging.udpV1;

import javax.inject.Inject;

public interface PhysicalNetworkLocation {

	@Override
	public boolean equals(Object other);

	@Override
	public int hashCode();
}
