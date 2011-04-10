package tahrir.io.net.udpV1;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.LoggerFactory;

import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.serialization.*;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.google.common.collect.*;
import com.google.gwt.thirdparty.guava.common.collect.MapMaker;

public class UdpRemoteConnection extends TrRemoteConnection<UdpRemoteAddress> implements
TrMessageListener<UdpRemoteAddress> {
	private final org.slf4j.Logger logger;

	private static final int MAX_RETRIES = 5;
	private volatile boolean disconnectedCallbackCalled = false;
	private final UdpNetworkInterface iface;

	private TrSymKey inboundSymKey;

	private ByteArraySegment inboundSymKeyEncoded = null;
	private final int KEEP_ALIVE_INTERVAL_SEC = 7;
	private final ScheduledFuture<?> keepAliveSender;
	private final TrSymKey outboundSymKey;

	private final Map<Integer, PendingLongMessage> pendingReceivedLongMessages = new MapMaker()
	.expiration(20, TimeUnit.MINUTES).makeMap();

	private final Set<Integer> recentlyReceivedShortMessages = Collections.newSetFromMap(new MapMaker().expiration(20,
			TimeUnit.MINUTES).<Integer, Boolean> makeMap());

	private boolean remoteHasCachedInboundKey = false;

	private final Map<Integer, Resender> resenders = new MapMaker().makeMap();

	private boolean shutdown = false;

	private boolean unregisterScheduled = false;

	protected UdpRemoteConnection(final UdpNetworkInterface iface, final UdpRemoteAddress remoteAddr,
			final RSAPublicKey remotePubKey, final TrMessageListener<UdpRemoteAddress> listener,
			final Runnable connectedCallback,
			final Runnable disconnectedCallback, final boolean unilateral) {
		super(remoteAddr, remotePubKey, listener, connectedCallback, disconnectedCallback, unilateral);
		this.iface = iface;
		logger = LoggerFactory.getLogger("UdpRemoteConnection(" + iface.config.listenPort + "-" + remoteAddress + ")");
		iface.registerListenerForSender(remoteAddr, this);
		outboundSymKey = TrCrypto.createAesKey();
		// logger.info("Using outboundSymKey: " +
		// Arrays.toString(outboundSymKey.toBytes()));
		if (unilateral) {
			inboundSymKey = outboundSymKey;
			inboundSymKeyEncoded = inboundSymKey.toByteArraySegment();
			remoteHasCachedInboundKey = true;
		}

		keepAliveSender = TrUtils.executor.schedule(new Runnable() {

			public void run() {
				final byte[] msg = new byte[1];
				msg[0] = PrimitiveMessageType.KEEPALIVE.id;
				final ByteArraySegment plainText = new ByteArraySegment(msg);
				final ByteArraySegment cipherText = encryptOutbound(plainText);
				iface.sendTo(remoteAddr, cipherText, TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
			}
		}, KEEP_ALIVE_INTERVAL_SEC, TimeUnit.SECONDS);
	}

	@Override
	public void disconnect() {
		if (!disconnectedCallbackCalled) {
			disconnectedCallbackCalled = true;
			disconnectedCallback.run();
		}
		keepAliveSender.cancel(false);
		shutdown = true;
		final byte[] msg = new byte[1];
		msg[0] = PrimitiveMessageType.SHUTDOWN.id;
		final ByteArraySegment plainText = new ByteArraySegment(msg);
		final ByteArraySegment cipherText = encryptOutbound(plainText);
		iface.sendTo(remoteAddress, cipherText, TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);

		if (!unregisterScheduled) {
			unregisterScheduled = true;
			TrUtils.executor.schedule(new Runnable() {

				public void run() {
					iface.unregisterListenerForSender(remoteAddress);
				}
			}, 60, TimeUnit.SECONDS);
		}
	}

	@Override
	public boolean isConnected() {
		return !shutdown && remoteHasCachedInboundKey;

	}

	public void received(final TrNetworkInterface<UdpRemoteAddress> iFace, final UdpRemoteAddress sender,
			ByteArraySegment message) {
		// logger.info("Message received from " + sender.port);
		if (inboundSymKey == null) {
			// We don't have the inbound sym key, but it will be prepended
			// to the message, 256 bytes
			inboundSymKeyEncoded = message.subsegment(0, 256);
			inboundSymKey = new TrSymKey(TrCrypto.decryptRaw(inboundSymKeyEncoded, iface.myPrivateKey));
			// logger.info("Received inboundSymKey: " +
			// Arrays.toString(inboundSymKey.toBytes()));
			message = message.subsegment(inboundSymKeyEncoded.length);
			// logger.info("Read inboundSymKey");
		} else if (message.startsWith(inboundSymKeyEncoded)) {
			// Sender is still prepending the inboundSymKey even though we
			// already have it, disregard it
			message = message.subsegment(inboundSymKeyEncoded.length);
			// logger.info("Message had inboundSymKey, but isn't needed");
		}
		// Decode the message
		try {
			message = inboundSymKey.decrypt(message);
			final DataInputStream dis = message.toDataInputStream();
			final PrimitiveMessageType type = PrimitiveMessageType.forBytes.get(dis.readByte());
			// logger.info(iface.config.listenPort + " received " + type +
			// " from " + remoteAddress);
			switch (type) {
			case ACK:
				if (!remoteHasCachedInboundKey) {
					// Receiving our first ACK indicates by-directional
					// communication is established
					remoteHasCachedInboundKey = true;
					connectedCallback.run();
				}
				if (shutdown) {
					disconnect();
				}
				final int messageId = dis.readInt();
				final Resender resender = resenders.remove(messageId);
				if (resender != null) {
					resender.receiptConfirmed = true;
					resender.callbacks.received();
				}
				break;
			case SHORT:
				if (shutdown) {
					disconnect();
				} else {
					handleShortMessage(dis, message.length);
				}
				break;
			case KEEPALIVE:
				if (shutdown) {
					disconnect();
				}
				break;
			case SHUTDOWN:
				disconnect();
				break;
			}
		} catch (final IOException e) {
			logger.error("Failed to handle message", e);
		} catch (final TrSerializableException e) {
			logger.error("Failed to handle message", e);
		}
	}

	@Override
	public void send(final ByteArraySegment message, final double priority, final TrSentReceivedListener sentListener)
	throws IOException {
		int estimatedPacketSize = 0;
		if (!remoteHasCachedInboundKey) {
			estimatedPacketSize += 256;
		}
		estimatedPacketSize += 5;
		estimatedPacketSize += message.length;
		if (estimatedPacketSize > UdpNetworkInterface.MAX_PACKET_SIZE_BYTES) {
			sendLongMessage(message, priority, sentListener);
		} else {
			// logger.info("Sending short message");
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			PrimitiveMessageType.SHORT.write(builder);
			final int messageId = TrUtils.rand.nextInt();
			builder.writeInt(messageId);
			ShortMessageType.SIMPLE.write(builder);
			builder.write(message);
			final Resender resender = new Resender(messageId, MAX_RETRIES, sentListener,
					encryptOutbound(builder.build()), this, priority);
			resenders.put(messageId, resender);
			resender.run();
		}
	}

	public void setRemotePubKey(final RSAPublicKey remotePubKey) {
		if (!remotePubKey.equals(this.remotePubKey))
			throw new UnsupportedOperationException("remotePubKey is already set to something else");
		this.remotePubKey = remotePubKey;
	}

	private ByteArraySegment encryptOutbound(final ByteArraySegment rawMessage) {
		final ByteArraySegmentBuilder toSend = ByteArraySegment.builder();
		if (!remoteHasCachedInboundKey) {
			if (remotePubKey == null) {
				System.out.println("Remote is null on " + iface.config.listenPort); // REMOVEME
			}
			toSend.write(TrCrypto.encryptRaw(outboundSymKey.toByteArraySegment(), remotePubKey));
		}
		toSend.write(outboundSymKey.encrypt(rawMessage));
		return toSend.build();
	}

	private void handleShortMessage(final DataInputStream dis, final int maxLength) throws IOException,
	TrSerializableException {
		final int messageId = dis.readInt();
		{
			// Construct and send an ack message
			final ByteArraySegmentBuilder ackMessage = ByteArraySegment.builder();
			PrimitiveMessageType.ACK.write(ackMessage);
			ackMessage.writeInt(messageId);
			iface.sendTo(remoteAddress, encryptOutbound(ackMessage.build()),
					TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
		}
		final ShortMessageType type = ShortMessageType.forBytes.get(dis.readByte());
		if (recentlyReceivedShortMessages.contains(messageId))
			// Seen this message before, disregard
			return;
		recentlyReceivedShortMessages.add(messageId);
		switch (type) {
		case SIMPLE:
			listener.received(iface, remoteAddress, ByteArraySegment.from(dis, maxLength));
			break;
		case LONG_PART:
			final LongPart lh = TrSerializer.deserializeFrom(LongPart.class, dis);
			// logger.info("Received " + lh);
			PendingLongMessage plm = pendingReceivedLongMessages.get(lh.longMessageId);
			if (plm == null) {
				plm = new PendingLongMessage(lh.totalParts);
				pendingReceivedLongMessages.put(lh.longMessageId, plm);
			}
			plm.parts[lh.partNumber] = lh.data;
			if (plm.isComplete()) {
				// logger.info("LongPart " + lh.longMessageId +
				// " received in its entirity");
				pendingReceivedLongMessages.remove(lh.longMessageId);
				final ByteArraySegmentBuilder longMessage = ByteArraySegment.builder();
				for (final ByteArraySegment bas : plm.parts) {
					longMessage.write(bas);
				}
				listener.received(iface, remoteAddress, longMessage.build());
			}
		}
	}

	private void sendLongMessage(final ByteArraySegment message, final double priority,
			final TrSentReceivedListener sentListener) throws IOException {
		final int packetSize = UdpNetworkInterface.MAX_PACKET_SIZE_BYTES - (remoteHasCachedInboundKey ? 60 : 316);
		final List<ByteArraySegment> segments = Lists.newArrayList();
		int startPos = 0;
		while (startPos < message.length) {
			segments.add(message.subsegment(startPos, packetSize));
			startPos += packetSize;
		}
		final int longMessageId = TrUtils.rand.nextInt();
		final boolean[] sent = new boolean[segments.size()];
		final boolean[] received = new boolean[segments.size()];
		for (int x = 0; x < segments.size(); x++) {
			final int pos = x;
			try {
				final LongPart lp = new LongPart(longMessageId, x, segments.size(), segments.get(x));
				final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
				PrimitiveMessageType.SHORT.write(builder);
				final int messageId = TrUtils.rand.nextInt();
				builder.writeInt(messageId);
				ShortMessageType.LONG_PART.write(builder);
				TrSerializer.serializeTo(lp, builder);
				final Resender resender = new Resender(messageId, MAX_RETRIES, new TrSentReceivedListener() {

					boolean failureReported = false;

					public void failure() {
						if (!failureReported) {
							failureReported = true;
							sentListener.failure();
						}
					}

					public void received() {
						received[pos] = true;
						// logger.info("Longpart " + pos +
						// " receive confirmation: " +
						// Arrays.toString(received));
						for (final boolean r : received) {
							if (!r)
								return;
						}
						sentListener.received();
					}

					public void sent() {
						sent[pos] = true;
						// logger.info("Longpart " + pos +
						// " sent confirmation: " + Arrays.toString(sent));
						for (final boolean s : sent) {
							if (!s)
								return;
						}
						sentListener.sent();
					}
				}, encryptOutbound(builder.build()), this, priority);
				resenders.put(messageId, resender);
				resender.run();
			} catch (final TrSerializableException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class LongPart {
		public ByteArraySegment data;
		public int longMessageId;
		public int partNumber;
		public int totalParts;

		public LongPart() {

		}

		public LongPart(final int longMessageId, final int partNumber, final int size, final ByteArraySegment data) {
			this.longMessageId = longMessageId;
			this.partNumber = partNumber;
			totalParts = size;
			this.data = data;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("LongPart [longMessageId=");
			builder.append(longMessageId);
			builder.append(", partNumber=");
			builder.append(partNumber);
			builder.append(", totalParts=");
			builder.append(totalParts);
			builder.append("]");
			return builder.toString();
		}
	}
	private static class PendingLongMessage {
		ByteArraySegment[] parts;

		public PendingLongMessage(final int length) {
			parts = new ByteArraySegment[length];
		}

		public boolean isComplete() {
			for (int x = 0; x < parts.length; x++) {
				if (parts[x] == null)
					return false;
			}
			return true;
		}
	}

	private enum PrimitiveMessageType {
		ACK(2), KEEPALIVE(3), SHORT(1), SHUTDOWN(4);

		public static Map<Byte, PrimitiveMessageType> forBytes;
		static {
			forBytes = Maps.newHashMap();
			for (final PrimitiveMessageType t : PrimitiveMessageType.values()) {
				forBytes.put(t.id, t);
			}
		}

		public final byte id;

		PrimitiveMessageType(final int id) {
			this.id = (byte) id;
		}

		public void write(final DataOutputStream dos) throws IOException {
			dos.writeByte(id);
		}
	}

	private static class Resender implements Runnable {
		private final TrSentReceivedListener callbacks;
		private final double initialPriority;
		private final int maxRetries;
		private final ByteArraySegment message;
		private final int messageId;
		private final UdpRemoteConnection parent;
		public volatile boolean receiptConfirmed = false;
		private int retryCount = 0;
		public Resender(final int messageId, final int maxRetries, final TrSentReceivedListener callbacks,
				final ByteArraySegment message, final UdpRemoteConnection parent, final double initialPriority) {
			this.messageId = messageId;
			this.maxRetries = maxRetries;
			this.callbacks = callbacks;
			this.message = message;
			this.parent = parent;
			this.initialPriority = initialPriority;
		}

		public void run() {
			final int thisRetryNo = retryCount;
			if (retryCount == maxRetries || receiptConfirmed || parent.shutdown) {
				parent.resenders.remove(messageId);
				if (retryCount == maxRetries || parent.shutdown) {
					callbacks.failure();
				}
			} else {
				parent.iface.sendTo(parent.remoteAddress, message, new TrSentListener() {

					public void failure() {
					}

					public void sent() {
						if (thisRetryNo == 0) {
							callbacks.sent();
						}
						TrUtils.executor.schedule(Resender.this, 5, TimeUnit.SECONDS);
					}
				}, thisRetryNo == 0 ? initialPriority : TrNetworkInterface.PACKET_RESEND_PRIORITY);
				retryCount++;
			}
		}

	}

	private enum ShortMessageType {
		LONG_PART(2), SIMPLE(0);

		public static Map<Byte, ShortMessageType> forBytes;
		static {
			forBytes = Maps.newHashMap();
			for (final ShortMessageType t : ShortMessageType.values()) {
				forBytes.put(t.id, t);
			}
		}

		public final byte id;

		ShortMessageType(final int id) {
			this.id = (byte) id;
		}

		public void write(final DataOutputStream dos) throws IOException {
			dos.writeByte(id);
		}
	}
}
