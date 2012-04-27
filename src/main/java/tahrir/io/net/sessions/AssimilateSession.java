package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;

import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.Capabilities;

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
	public void requestNewConnection(PhysicalNetworkLocation requestor, final RSAPublicKey requestorPubkey);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void acceptNewConnection(PhysicalNetworkLocation acceptor, final RSAPublicKey acceptorPubkey);

	@Priority(TrNetworkInterface.ASSIMILATION_PRIORITY)
	public void myCapabilitiesAre(Capabilities myCapabilities);
}
