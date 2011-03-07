package tahrir.io.net.udp;

import java.net.InetAddress;
import java.security.interfaces.RSAPublicKey;

import tahrir.io.net.TrRemoteConnection;

public class UdpConnection extends TrRemoteConnection {

	public final RSAPublicKey connPubkey;
	public final InetAddress address;
	private final UdpConnectionManager manager;

	protected UdpConnection(final UdpConnectionManager manager, final RSAPublicKey connPubkey, final InetAddress address) {
		this.manager = manager;
		this.connPubkey = connPubkey;
		this.address = address;

	}


}