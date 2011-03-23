package tahrir.io.net.udp;

import java.net.*;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.*;

import tahrir.TrConstants;
import tahrir.io.crypto.*;
import tahrir.io.net.TrRemoteConnection;
import tahrir.io.serialization.*;
import tahrir.tools.TrUtils;

public class UdpConnection extends TrRemoteConnection {

	public final RSAPublicKey connPubkey;
	public final InetAddress address;
	private final UdpConnectionManager manager;
	private final TrSymKey receiveSymKey;
	private final ScheduledFuture<?> connInitFuture;
	private final ByteBuffer encryptedReceiveSymKeyBB;
	private final int port;

	protected UdpConnection(final UdpConnectionManager manager, final RSAPublicKey connPubkey,
			final InetAddress address, final int port) throws TrSerializableException {
		this.manager = manager;
		this.connPubkey = connPubkey;
		this.address = address;
		this.port = port;

		receiveSymKey = TrCrypto.createAesKey();

		final TrPPKEncrypted<TrSymKey> encryptedReceiveSymKey = TrCrypto.encrypt(receiveSymKey, connPubkey);

		encryptedReceiveSymKeyBB = ByteBuffer.allocate(TrConstants.MAX_BYTEBUFFER_SIZE_BYTES);

		TrSerializer.serializeTo(encryptedReceiveSymKey, encryptedReceiveSymKeyBB);

		encryptedReceiveSymKeyBB.flip();

		connInitFuture = TrUtils.executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				final DatagramPacket packet = new DatagramPacket(encryptedReceiveSymKeyBB.array(),
						encryptedReceiveSymKeyBB.position(), encryptedReceiveSymKeyBB.remaining(), address, port);
				manager.sendQueue.add(new PrioritizedUdpPacket(packet, PrioritizedUdpPacket.CONNECTION_MAINTAINANCE));
			}
		}, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public void send(final byte[] data) throws WrongStateException {
		// TODO Auto-generated method stub

	}

}