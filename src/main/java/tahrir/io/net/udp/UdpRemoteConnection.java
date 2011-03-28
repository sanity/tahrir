package tahrir.io.net.udp;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.concurrent.*;

import org.slf4j.*;

import tahrir.TrConstants;
import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.serialization.*;
import tahrir.tools.TrUtils;

public class UdpRemoteConnection extends TrRemoteConnection<UdpRemoteAddress> implements
TrNetworkInterface.TrMessageListener<UdpRemoteAddress> {
	final Logger logger = LoggerFactory.getLogger(UdpRemoteConnection.class);

	private final UdpNetworkInterface iface;
	private final UdpRemoteAddress address;
	private final RSAPublicKey pubKey;
	private final TrSymKey inboundSymKey;
	private TrSymKey outboundSymKey = null;
	private byte[] remoteSymKeyMsg = null;
	private final ScheduledFuture<?> connectionInitiationSender;
	private boolean inboundConnectivityEstablished = false, outboundConnectivityEstablished = false;

	public static final int INTRODUCE = 0;

	protected UdpRemoteConnection(final UdpNetworkInterface iface, final UdpRemoteAddress address,
			final RSAPublicKey pubKey) {
		this.iface = iface;
		this.address = address;
		this.pubKey = pubKey;
		inboundSymKey = TrCrypto.createAesKey();
		final byte[] encryptedSymKeys = TrCrypto.encryptRaw(inboundSymKey.toBytes(), pubKey);

		iface.registerListenerForSender(address, this);

		connectionInitiationSender = TrUtils.executor.scheduleWithFixedDelay(new Runnable() {

			public void run() {
				iface.sendTo(address, encryptedSymKeys, TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
			}
		}, 0, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	public void received(final TrNetworkInterface<UdpRemoteAddress> iFace, final UdpRemoteAddress sender,
			final byte[] message, final int length) {
		if (getState().equals(State.CONNECTING)) {
			if (remoteSymKeyMsg == null || Arrays.equals(remoteSymKeyMsg, message)) {
				// We don't know the remote connection's symkey yet so assume this
				// is it
				if (!inboundConnectivityEstablished) {
					inboundConnectivityEstablished = true;
					remoteSymKeyMsg = message;
					outboundSymKey = new TrSymKey(TrCrypto.decryptRaw(message, length, iface.myPrivateKey));
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
					final byte[] ciphertext = outboundSymKey.encrypt(plaintext);
					iface.sendTo(sender, ciphertext,
							TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				} catch (final TrSerializableException e) {
					throw new RuntimeException(e);
				}
			} else {
				final byte[] plainText = inboundSymKey.decrypt(message, length);
				final ByteArrayInputStream bais = new ByteArrayInputStream(plainText);
				final DataInputStream dis = new DataInputStream(bais);

				try {
					final int messageType = dis.readByte();

					if (messageType == INTRODUCE) {
						outboundConnectivityEstablished = true;
						connectionInitiationSender.cancel(false);
					}
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (inboundConnectivityEstablished && outboundConnectivityEstablished) {
				changeStateTo(State.CONNECTED);
			}
		}
	}

	public static class Introduce {
		public String version;
	}
}
