package tahrir.network.sessions;

import java.security.interfaces.RSAPublicKey;

import tahrir.network.PhysicalNetworkLocation;
import tahrir.network.RemoteNodeAddress;
import tahrir.network.TrNetworkInterface;
import tahrir.network.TrPeerManager.Capabilities;
import tahrir.network.TrSession;

/*
 * This is the assimilation procedure, we have a requestor, who needs a new connection, zero or more
 * relays, who will forward the request, and the acceptor - who agrees to connect to the requestor
 * 
 * requestor -> relay1 : requestNewConnection()
 * requestor <- relay1 : yourAddressIs()
 * relay1 -> relay2 : requestNewConnection(requestor)
 * relay2 -> acceptor : requestNewConnection(requestor)
 * relay2 <- acceptor acceptNewConnection()
 * requestor <- acceptor : myCapabilitiesAre()
 * relay1 <- relay2 : acceptNewConnection(acceptor)
 * requestor <- relay1 : acceptNewconnection(acceptor)
 * requestor -> acceptor : myCapabilitiesAre()
 */

public interface AssimilateSession extends TrSession {
	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void yourAddressIs(final PhysicalNetworkLocation address);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void requestNewConnection(final RSAPublicKey requestorPubkey);

    @Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
    public void requestNewConnection(RemoteNodeAddress requestorAddress, int UId);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void acceptNewConnection(RemoteNodeAddress acceptorAddress);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void myCapabilitiesAre(Capabilities myCapabilities, int topologyLocation);

    @Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
    public void rejectAlreadySeen(int uId);
}
