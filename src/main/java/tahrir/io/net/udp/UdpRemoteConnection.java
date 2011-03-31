package tahrir.io.net.udp;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.Map.Entry;
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
import com.google.common.collect.*;

public class UdpRemoteConnection extends TrRemoteConnection<UdpRemoteAddress> implements
TrNetworkInterface.TrMessageListener<UdpRemoteAddress> {
	final Logger logger = LoggerFactory.getLogger(UdpRemoteConnection.class);

	private final UdpNetworkInterface iface;
	private final UdpRemoteAddress address;
	private final RSAPublicKey pubKey;
	private final TrSymKey inboundSymKey;
	private TrSymKey outboundSymKey = null;
	private final ScheduledFuture<?> connectionInitiationSender;
	private boolean inboundConnectivityEstablished = false, outboundConnectivityEstablished = false;

	public enum MessageType {
		INTRODUCE(0), SHORT(1), LONG_HEADER(2), ACK(3);

		public static Map<Byte, MessageType> forBytes;
		static {
			forBytes = Maps.newHashMap();
			for (final MessageType t : MessageType.values()) {
				forBytes.put(t.id, t);
			}
		}

		public final byte id;

		MessageType(final int id) {
			this.id = (byte) id;
		}

		public void write(final DataOutputStream dos) throws IOException {
			dos.writeByte(id);
		}
	}

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
		if (!getState().equals(State.CONNECTED))
			throw new RuntimeException("Not connected");
		if (message.length < TrConstants.MAX_UDP_PACKET_SIZE - 10) {
			sendShortMessage(message, priority, sentListener, msgUid, MessageType.SHORT);
		} else {
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			final int maxBlockSize = TrConstants.MAX_UDP_PACKET_SIZE - 15;
			final Map<Integer, ByteArraySegment> toSend = Maps.newHashMap();
			int offset = 0;
			while (offset < message.length) {
				final int sUid = TrUtils.rand.nextInt();
				// This is safe because subsegment will truncate if it goes over
				toSend.put(sUid, message.subsegment(offset, maxBlockSize));
				offset += maxBlockSize;
			}
			final Set<Integer> unconfirmed = Sets.newHashSet(toSend.keySet());
			for (final int sUid : toSend.keySet()) {
				builder.writeInt(sUid);
			}
			sendShortMessage(builder.build(), TrNetworkInterface.LONG_MESSAGE_HEADER, new TrSentReceivedListener() {

				public void sent() {
				}

				public void failure() {
					sentListener.failure();
				}

				public void received() {
					// The header has been received, so now send the payload
					// packets
					for (final Entry<Integer, ByteArraySegment> e : toSend.entrySet()) {
						try {
							UdpRemoteConnection.this.send(e.getValue(), priority, new TrSentReceivedListener() {

								public void sent() {
									// Sending individual component packages
									// isn't noteworthy
								}

								public void failure() {
									sentListener.failure();
								}

								public void received() {
									unconfirmed.remove(e.getKey());
									if (unconfirmed.isEmpty()) {
										sentListener.sent();
										sentListener.received();
									}
								}
							}, e.getKey());
						} catch (final IOException e1) {
							throw new RuntimeException(e1);
						}
					}
				}
			}, msgUid, MessageType.LONG_HEADER);
		}
	}

	protected void sendShortMessage(final ByteArraySegment message, final double priority,
			final TrSentReceivedListener sentListener, final int msgUid, final MessageType s) throws IOException {
		final ByteArraySegmentBuilder dos = ByteArraySegment.builder();
		s.write(dos);
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
					outboundSymKey = new TrSymKey(TrCrypto.decryptRaw(message, iface.myPrivateKey));
				}
				final ByteArraySegmentBuilder dos = ByteArraySegment.builder();
				try {
					MessageType.INTRODUCE.write(dos);
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
					final MessageType messageType = MessageType.forBytes.get(dis.readByte());

					if (messageType.equals(MessageType.INTRODUCE)) {
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
				final MessageType messageType = MessageType.forBytes.get(dis.readByte());

				if (messageType.equals(MessageType.ACK)) {
					final int msgUid = dis.readInt();
					final Tuple2<ScheduledFuture<?>, TrSentReceivedListener> resendDat = awaitingAcks.remove(msgUid);
					if (resendDat != null) {
						resendDat.a.cancel(false);
						resendDat.b.received();
					}
				}

				if (messageType.equals(MessageType.SHORT) || messageType.equals(MessageType.LONG_HEADER)) {
					final int msgUid = dis.readInt();
					// Ack the message (even if we're already received it
					// before, so that the sender stops resending - this could
					// happen if the ack was dropped)
					{
						final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
						MessageType.ACK.write(builder);
						builder.writeInt(msgUid);
						builder.flush();
						iface.sendTo(address, outboundSymKey.encrypt(builder.build()),
								TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
					}
					// Ignore if we've received it before
					if (!recentlyReceivedUids.containsKey(msgUid)) {
						if (messageType.equals(MessageType.SHORT)) {
							final byte[] payloadBA = new byte[dis.readInt()];
							final int actuallyRead = dis.read(payloadBA);
							final ByteArraySegment payload = new ByteArraySegment(payloadBA);
							if (actuallyRead != payload.length)
								throw new RuntimeException("Packet length "+actuallyRead+", but expected "+payload.length);

							final PendingLongMessage plm = awaitingLongPackets.get(msgUid);
							if (plm != null) {
								// This is part of a long packet, handle accordingly
								plm.receivedPackets.put(msgUid, payload);
								if (plm.receivedPackets.size() == plm.packetOrder.size()) {
									final ByteArraySegmentBuilder longPacket = ByteArraySegment.builder();
									// We've got them all, remove the PLMs and build the long packet
									for (final int p : plm.packetOrder) {
										awaitingLongPackets.remove(p);
										longPacket.write(plm.receivedPackets.get(p));
									}
									listener.received(iface, sender, longPacket.build());
								}
							} else {
								listener.received(iface, sender, payload);
							}
						} else if (messageType.equals(MessageType.LONG_HEADER)) {
							final PendingLongMessage plm = new PendingLongMessage();
							plm.packetOrder = new ConcurrentLinkedQueue<Integer>();
							plm.receivedPackets = new ConcurrentHashMap<Integer, ByteArraySegment>();
							final int messageLength = dis.readInt();
							final int packetCount = messageLength / 4;
							for (int x = 0; x < packetCount; x++) {
								final int sUid = dis.readInt();
								plm.packetOrder.add(sUid);
								awaitingLongPackets.put(sUid, plm);
							}
						}
					}

				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected Map<Integer, PendingLongMessage> awaitingLongPackets = new MapMaker().expiration(20, TimeUnit.MINUTES)
	.makeMap();

	public static class PendingLongMessage {
		ConcurrentLinkedQueue<Integer> packetOrder;

		Map<Integer, ByteArraySegment> receivedPackets;
	}

	public static class Introduce {
		public String version;
	}
}
