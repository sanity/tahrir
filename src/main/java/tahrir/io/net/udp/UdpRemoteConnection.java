package tahrir.io.net.udp;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.concurrent.*;

import tahrir.TrConstants;
import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.serialization.*;
import tahrir.tools.TrUtils;

public class UdpRemoteConnection extends TrRemoteConnection implements
TrNetworkInterface.TrMessageListener<TrRemoteAddress> {
	private final UdpNetworkInterface iface;
	private final UdpRemoteAddress address;
	private final RSAPublicKey pubKey;
	private final TrSymKey localConnSymKey;
	private TrSymKey remoteConnSymKey = null;
	private byte[] remoteSymKeyMsg = null;
	private final ScheduledFuture<?> connectionInitiationSender;

	public static final int INTRODUCE = 0;

	protected UdpRemoteConnection(final UdpNetworkInterface iface, final UdpRemoteAddress address,
			final RSAPublicKey pubKey) {
		this.iface = iface;
		this.address = address;
		this.pubKey = pubKey;
		localConnSymKey = TrCrypto.createAesKey();
		final byte[] encryptedSymKeys = TrCrypto.encryptRaw(localConnSymKey.toBytes(), pubKey);
		connectionInitiationSender = TrUtils.executor.scheduleWithFixedDelay(new Runnable() {

			public void run() {
				iface.sendTo(address, encryptedSymKeys, TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
			}
		}, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	public void received(final TrNetworkInterface<TrRemoteAddress> iFace, final TrRemoteAddress sender,
			final byte[] message, final int length) {
		if (getState().equals(State.CONNECTING)) {
			if (remoteSymKeyMsg == null || Arrays.equals(remoteSymKeyMsg, message)) {
				// We don't know the remote connection's symkey yet so assume this
				// is it
				if (remoteConnSymKey == null) {
					remoteSymKeyMsg = message;
					remoteConnSymKey = new TrSymKey(TrCrypto.decryptRaw(message, length, iface.myPrivateKey));
				}
				final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				final DataOutputStream dos = new DataOutputStream(baos);
				try {
					dos.writeByte(INTRODUCE);
					final Introduce obj = new Introduce();
					obj.version = TrConstants.version;
					TrSerializer.serializeTo(obj, dos);
					dos.flush();
					final byte[] plaintext = baos.toByteArray();
					final byte[] ciphertext = remoteConnSymKey.encrypt(plaintext);

				} catch (final IOException e) {
					throw new RuntimeException(e);
				} catch (final TrSerializableException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static class Introduce {
		public String version;
	}
}
