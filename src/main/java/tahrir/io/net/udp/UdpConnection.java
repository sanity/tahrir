package tahrir.io.net.udp;

import java.io.*;
import java.net.*;
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
	private final byte[] encryptedReceiveSymKeyBA;
	private final int port;

	protected UdpConnection(final UdpConnectionManager manager, final RSAPublicKey connPubkey,
			final InetAddress address, final int port) throws TrSerializableException, IOException {
		this.manager = manager;
		this.connPubkey = connPubkey;
		this.address = address;
		this.port = port;

		receiveSymKey = TrCrypto.createAesKey();

		final TrPPKEncrypted<TrSymKey> encryptedReceiveSymKey = TrCrypto.encrypt(receiveSymKey, connPubkey);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream(TrConstants.DEFAULT_BAOS_SIZE);
		final DataOutputStream dos = new DataOutputStream(baos);
		TrSerializer.serializeTo(encryptedReceiveSymKey, dos);
		dos.flush();
		encryptedReceiveSymKeyBA = baos.toByteArray();

		connInitFuture = TrUtils.executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				final DatagramPacket packet = new DatagramPacket(encryptedReceiveSymKeyBA,
						encryptedReceiveSymKeyBA.length, address, port);
				manager.sendQueue.add(new PrioritizedUdpPacket(packet, PrioritizedUdpPacket.CONNECTION_MAINTAINANCE));
			}
		}, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public void send(final byte[] data) throws WrongStateException {
		// TODO Auto-generated method stub

	}

}