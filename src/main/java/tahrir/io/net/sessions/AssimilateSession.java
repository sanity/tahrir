package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;

import tahrir.io.net.*;

public interface AssimilateSession extends TrSession {

	public void yourAddressIs(final TrRemoteAddress address);

	public void requestNewConnection(final RSAPublicKey requestorPubkey);

	public void requestNewConnection(TrRemoteAddress requestor, final RSAPublicKey requestorPubkey);
}
