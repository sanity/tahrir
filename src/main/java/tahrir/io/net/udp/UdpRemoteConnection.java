package tahrir.io.net.udp;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import tahrir.TrConstants;
import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.serialization.*;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.MapMaker;

public class UdpRemoteConnection extends TrRemoteConnection<UdpRemoteAddress> implements
TrNetworkInterface.TrMessageListener<UdpRemoteAddress> {
	final Logger logger = LoggerFactory.getLogger(UdpRemoteConnection.class);

	private final UdpNetworkInterface iface;
	private final UdpRemoteAddress address;
	private final RSAPublicKey pubKey;
	private final TrSymKey inboundSymKey;
	private TrSymKey outboundSymKey = null;
	private ByteArraySegment remoteSymKeyMsg = null;
	private final ScheduledFuture<?> connectionInitiationSender;
	private boolean inboundConnectivityEstablished = false, outboundConnectivityEstablished = false;

	public static final int INTRODUCE = 0;
	public static final int SHORT_MESSAGE = 1;
	public static final int LONG_MESSAGE_HEADER = 2;
	public static final int MESSAGE_ACK = 3;

	private static final int MAX_INTRODUCE_LENGTH_BYTES = 100;

	protected ConcurrentHashMap<Integer, Tuple2<ScheduledFuture<?>, TrSentReceivedListener>> awaitingAcks = new ConcurrentHashMap<Integer, Tuple2<ScheduledFuture<?>, TrSentReceivedListener>>();

	protected ConcurrentMap<Integer, RecentlyReceivedMessage> recentlyReceivedUids = new MapMaker().expiration(20,
			TimeUnit.MINUTES).makeMap();

	private final TrMessageListener<UdpRemoteAddress> listener;

	protected static class RecentlyReceivedMessage {
		long time;
		int uid;
	}

	protected UdpRemoteConnection(final UdpNetworkInterface iface, final UdpRemoteAddress address,
			final RSAPublicKey pubKey, final TrMessageListener<UdpRemoteAddress> listener) {
		this.iface = iface;
		this.address = address;
		this.pubKey = pubKey;
		this.listener = listener;
		inboundSymKey = TrCrypto.createAesKey();
		final ByteArraySegment encryptedSymKeys = TrCrypto.encryptRaw(inboundSymKey.toByteArraySegment(), pubKey);

		// We use the length of the message to indicate whether it is the
		// connection initiation or an introduce message
		assert encryptedSymKeys.length > MAX_INTRODUCE_LENGTH_BYTES;

		iface.registerListenerForSender(address, this);

		connectionInitiationSender = TrUtils.executor.scheduleWithFixedDelay(new Runnable() {

			public void run() {
				iface.sendTo(address, encryptedSymKeys, TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
			}
		}, 0, TrConstants.UDP_CONN_INIT_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	public void send(final ByteArraySegment message, final double priority, final TrSentReceivedListener sentListener)
	throws IOException {
		send(message, priority, sentListener, TrUtils.rand.nextInt());
	}

	public void send(final ByteArraySegment message, final double priority, final TrSentReceivedListener sentListener,
			final int msgUid) throws IOException {
		if (message.length < TrConstants.MAX_UDP_PACKET_SIZE - 10) {
			final ByteArraySegmentBuilder dos = ByteArraySegment.builder();
			dos.writeByte(SHORT_MESSAGE);
			dos.writeInt(msgUid);
			dos.writeInt(message.length);
			message.writeTo(dos);
			dos.flush();
			final ByteArraySegment encryptedMessage = outboundSymKey.encrypt(dos.build());
			iface.sendTo(address, encryptedMessage, new TrSentListener() {

				public void sent() {
					sentListener.sent();
					final ScheduledFuture<?> ackResendFuture = TrUtils.executor.scheduleAtFixedRate(new Runnable() {
						int resendCount = 0;

						public void run() {
							resendCount++;
							if (resendCount > TrConstants.UDP_SHORT_MESSAGE_RETRY_ATTEMPTS) {
								awaitingAcks.remove(msgUid).a.cancel(false);
								sentListener.failure();
							} else {
								iface.sendTo(address, encryptedMessage, TrNetworkInterface.PACKET_RESEND_PRIORITY);
							}
						}
					}, TrConstants.DEFAULT_UDP_ACK_TIMEOUT_MS, TrConstants.DEFAULT_UDP_ACK_TIMEOUT_MS,
					TimeUnit.MILLISECONDS);
					awaitingAcks.put(msgUid, new Tuple2<ScheduledFuture<?>, TrSentReceivedListener>(ackResendFuture,
							sentListener));
				}

				public void failure() {
					sentListener.failure();
				}
			}, priority);
		} else {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(message.length * 2);
			final DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(LONG_MESSAGE_HEADER);
			dos.writeInt(msgUid);
			final int maxBlockSize = TrConstants.MAX_UDP_PACKET_SIZE - 15;
			final Map<Integer, byte[]> toSend = Maps.newHashMap();
			// final ByteArrayInputStream bais = new
			// ByteArrayInputStream(message);

		}
	}

	private static boolean arrayEquals(final byte[] array1, final byte[] array2, final int length) {
		System.err.println("Check array sameness:");
		System.err.println(Arrays.toString(array1));
		System.err.println(Arrays.toString(array2));
		for (int x = 0; x < length; x++) {
			if (array1[x] != array2[x])
				return false;
		}
		System.err.println("Same!");
		return true;
	}

	@Override
	public void received(final TrNetworkInterface<UdpRemoteAddress> iFace, final UdpRemoteAddress sender,
			final ByteArraySegment message) {
		if (getState().equals(State.CONNECTING)) {
			if (message.length > MAX_INTRODUCE_LENGTH_BYTES) {
				// We don't know the remote connection's symkey yet so assume this
				// is it
				if (!inboundConnectivityEstablished) {
					inboundConnectivityEstablished = true;
					remoteSymKeyMsg = message;
					outboundSymKey = new TrSymKey(TrCrypto.decryptRaw(message, iface.myPrivateKey));
				}
				final ByteArraySegmentBuilder dos = ByteArraySegment.builder();
				try {
					dos.writeByte(INTRODUCE);
					final Introduce obj = new Introduce();
					obj.version = TrConstants.version;
					TrSerializer.serializeTo(obj, dos);
					dos.flush();
					final ByteArraySegment ciphertext = outboundSymKey.encrypt(dos.build());
					assert ciphertext.length <= MAX_INTRODUCE_LENGTH_BYTES;
					iface.sendTo(sender, ciphertext,
							TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				} catch (final TrSerializableException e) {
					throw new RuntimeException(e);
				}
			} else {
				final ByteArraySegment plainText = inboundSymKey.decrypt(message);
				final DataInputStream dis = plainText.toDataInputStream();

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
		} else if (getState().equals(State.CONNECTED)) {
			final ByteArraySegment plainText = inboundSymKey.decrypt(message);
			final DataInputStream dis = plainText.toDataInputStream();

			try {
				final int messageType = dis.readByte();

				if (messageType == MESSAGE_ACK) {
					final int msgUid = dis.readInt();
					final Tuple2<ScheduledFuture<?>, TrSentReceivedListener> resendDat = awaitingAcks.remove(msgUid);
					if (resendDat != null) {
						resendDat.a.cancel(false);
						resendDat.b.received();
					}
				}

				if (messageType == SHORT_MESSAGE) {
					final int msgUid = dis.readInt();
					// Ack the message (even if we're already received it
					// before, so that the sender stops resending - this could
					// happen if the ack was dropped)
					{
						final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
						builder.writeByte(MESSAGE_ACK);
						builder.writeInt(msgUid);
						builder.flush();
						iface.sendTo(address, outboundSymKey.encrypt(builder.build()),
								TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
					}
					// Ignore if we've received it before
					if (!recentlyReceivedUids.containsKey(msgUid)) {
						final byte[] payload = new byte[dis.readInt()];
						final int actuallyRead = dis.read(payload);
						if (actuallyRead != payload.length)
							throw new RuntimeException("Packet length "+actuallyRead+", but expected "+payload.length);
						listener.received(iface, sender, new ByteArraySegment(payload));
					}

				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class Introduce {
		public String version;
	}
}
