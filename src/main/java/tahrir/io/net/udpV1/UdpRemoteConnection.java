package tahrir.io.net.udpV1;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import tahrir.TrConstants;
import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.net.TrNetworkInterface.TrMessageListener;
import tahrir.io.net.TrNetworkInterface.TrSentListener;
import tahrir.io.net.TrNetworkInterface.TrSentReceivedListener;
import tahrir.io.serialization.*;
import tahrir.tools.*;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

import com.google.common.base.Function;
import com.google.common.collect.*;

public class UdpRemoteConnection extends TrRemoteConnection {
	private volatile boolean disconnectedCallbackCalled = false;
	private final UdpNetworkInterface iface;
	private TrSymKey inboundSymKey;

	private ByteArraySegment inboundSymKeyEncoded = null;

	private final ScheduledFuture<?> keepAliveSender;
	private final org.slf4j.Logger logger;
	private TrSymKey outboundSymKey;

	private final Map<Integer, PendingLongMessage> pendingReceivedLongMessages = new MapMaker()
	.expiration(20, TimeUnit.MINUTES).makeMap();

	private final Set<Integer> recentlyReceivedShortMessages = Collections.newSetFromMap(new MapMaker().expiration(20,
			TimeUnit.MINUTES).<Integer, Boolean> makeMap());

	private boolean remoteHasCachedOurOutboundSymKey = false;

	private final Map<Integer, Resender> resenders = new MapMaker().makeMap();

	private boolean shutdown = false;

	private boolean unregisterScheduled = false;

	protected UdpRemoteConnection(
			final UdpNetworkInterface iface,
			final UdpNetworkLocation remoteAddr,
			final RSAPublicKey remotePubKey,
			final TrMessageListener listener,
			final Function<TrRemoteConnection, Void> connectedCallback,
			final Runnable disconnectedCallback,
			final boolean unilateralOutbound) {
		super(remoteAddr, remotePubKey, listener, connectedCallback, disconnectedCallback, unilateralOutbound);
		this.iface = iface;
		logger = LoggerFactory.getLogger(UdpRemoteConnection.class.getName()+" ("+iface.config.listenPort+">"+remoteAddr.port+")");
		logger.debug("Created");

		if (remotePubKey != null) {
			outboundSymKey = TrCrypto.createAesKey();
			//			inboundSymKey = outboundSymKey;
			//			inboundSymKeyEncoded = inboundSymKey.toByteArraySegment();
			//		remoteHasCachedOurOutboundSymKey = true;
		} else {
			// If we don't know the remote's public key this must be a
			// unilateral inbound connection, and we will be using the
			// inboundSymKey (once we are told it) to encrypt outbound
			// messages too
			if (unilateralOutbound)
				throw new RuntimeException("remotePubKey can't be null for a unilateralOutbound connection");
		}

		if (unilateralOutbound) {
			// If it's unilateral outbound, then the other side will
			// encrypt its reply with the outboundSymKey we provide
			inboundSymKey = outboundSymKey;
		}

		keepAliveSender = TrUtils.executor.schedule(new Runnable() {

			public void run() {
				// Warning: We're assuming that the other node has cached
				// our outboundSymKey here.  Save assumption?  Not sure :-/
				final byte[] msg = new byte[1];
				msg[0] = PrimitiveMessageType.KEEPALIVE.id;
				final ByteArraySegment plainText = new ByteArraySegment(msg);
				final ByteArraySegment cipherText = encryptOutbound(plainText);
				iface.sendTo(remoteAddr, cipherText, TrNetworkInterface.CONNECTION_MAINTAINANCE_PRIORITY);
			}
		}, TrConstants.UDP_KEEP_ALIVE_DURATION, TimeUnit.SECONDS);
	}

	@Override
	public void disconnect() {
		logger.debug("disconnect() called");
		if (!disconnectedCallbackCalled) {
			disconnectedCallbackCalled = true;
			if (disconnectedCallback != null) {
				disconnectedCallback.run();
			}
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
					logger.debug("Removing connection from parent after 60 second delay");
					iface.remoteConnections.remove(remoteAddress);
				}
			}, 60, TimeUnit.SECONDS);
		}
	}

	@Override
	public boolean isConnected() {
		return !shutdown && remoteHasCachedOurOutboundSymKey;

	}

	public void received(final TrNetworkInterface iFace, final PhysicalNetworkLocation sender_,
			ByteArraySegment message) {
		logger.debug("Received message from "+sender_);
		final UdpNetworkLocation sender = (UdpNetworkLocation) sender_;
		if (inboundSymKey == null) {
			logger.debug("We don't know the inboundSymKey yet, looking for it to be pre-pended to message");
			inboundSymKeyEncoded = message.subsegment(0, 256);
			inboundSymKey = new TrSymKey(TrCrypto.decryptRaw(inboundSymKeyEncoded, iface.myPrivateKey));
			logger.debug("decoded inboundSymKey");

			if (isUnilateralInbound()) {
				logger.debug("Unilateral inbound, so we use the inboundSymKey to encrypt outbound messages too, and we know remote has cached it");
				outboundSymKey = inboundSymKey;
				remoteHasCachedOurOutboundSymKey = true;
			}
			message = message.subsegment(inboundSymKeyEncoded.length);
		} else if (!unilateralOutbound && message.startsWith(inboundSymKeyEncoded)) {
			// Sender is still prepending the inboundSymKey even though we
			// already have it, disregard it
			logger.debug("Sender prepended the inboundSymKey even though we already have it");
			message = message.subsegment(inboundSymKeyEncoded.length);
		}
		// Decode the message
		try {
			logger.debug("Decoding message");
			message = inboundSymKey.decrypt(message);

			if (!remoteHasCachedOurOutboundSymKey && unilateralOutbound) {
				logger.debug("If this is a response to a unilateral message, we know remote has our outboundSymKey");
				remoteHasCachedOurOutboundSymKey = true;
			}

			final DataInputStream dis = message.toDataInputStream();
			final PrimitiveMessageType type = PrimitiveMessageType.forBytes.get(dis.readByte());
			switch (type) {
			case ACK:
				if (!remoteHasCachedOurOutboundSymKey) {
					logger.debug("Received first ACK, we know remote has cached our outboundSymKey");
					// Receiving our first ACK indicates by-directional
					// communication is established
					remoteHasCachedOurOutboundSymKey = true;
					if (connectedCallback != null) {
						connectedCallback.apply(this);
					}
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
		if (!remoteHasCachedOurOutboundSymKey) {
			estimatedPacketSize += 256;
		}
		estimatedPacketSize += 5;
		estimatedPacketSize += message.length;
		if (estimatedPacketSize > TrConstants.MAX_UDP_PACKET_SIZE) {
			sendLongMessage(message, priority, sentListener);
		} else {
			// logger.debug("Sending short message");
			final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
			PrimitiveMessageType.SHORT.write(builder);
			final int messageId = TrUtils.rand.nextInt();
			builder.writeInt(messageId);
			ShortMessageType.SIMPLE.write(builder);
			builder.write(message);
			final Resender resender = new Resender(messageId, TrConstants.UDP_SHORT_MESSAGE_RETRY_ATTEMPTS, sentListener,
					encryptOutbound(builder.build()), this, priority);
			resenders.put(messageId, resender);
			resender.run();
		}
	}

	private ByteArraySegment encryptOutbound(final ByteArraySegment rawMessage) {
		final ByteArraySegmentBuilder toSend = ByteArraySegment.builder();
		if (!remoteHasCachedOurOutboundSymKey) {
			logger.debug("Remote hasn't yet cached our outboundSymKey, prepend it");
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
			logger.debug("Sending ACK");
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
			// logger.debug("Received " + lh);
			PendingLongMessage plm = pendingReceivedLongMessages.get(lh.longMessageId);
			if (plm == null) {
				plm = new PendingLongMessage(lh.totalParts);
				pendingReceivedLongMessages.put(lh.longMessageId, plm);
			}
			plm.parts[lh.partNumber] = lh.data;
			if (plm.isComplete()) {
				// logger.debug("LongPart " + lh.longMessageId +
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
		final int packetSize = TrConstants.MAX_UDP_PACKET_SIZE - (remoteHasCachedOurOutboundSymKey ? 60 : 316);
		final List<ByteArraySegment> segments = Lists.newArrayList();
		int startPos = 0;
		while (startPos < message.length) {
			segments.add(message.subsegment(startPos, packetSize));
			startPos += packetSize;
		}
		final int longMessageId = TrUtils.rand.nextInt();
		final ArrayList<AtomicBoolean> sent = Lists.newArrayListWithCapacity(segments.size());
		final ArrayList<AtomicBoolean> received = Lists.newArrayListWithCapacity(segments.size());
		for (int x = 0; x < segments.size(); x++) {
			sent.add(x, new AtomicBoolean(false));
			received.add(x, new AtomicBoolean(false));

			final int pos = x;
			try {
				final LongPart lp = new LongPart(longMessageId, x, segments.size(), segments.get(x));
				final ByteArraySegmentBuilder builder = ByteArraySegment.builder();
				PrimitiveMessageType.SHORT.write(builder);
				final int messageId = TrUtils.rand.nextInt();
				builder.writeInt(messageId);
				ShortMessageType.LONG_PART.write(builder);
				TrSerializer.serializeTo(lp, builder);
				final Resender resender = new Resender(messageId, TrConstants.UDP_SHORT_MESSAGE_RETRY_ATTEMPTS, new TrSentReceivedListener() {

					boolean failureReported = false;

					public void failure() {
						if (!failureReported) {
							failureReported = true;
							sentListener.failure();
						}
					}

					public void received() {
						received.get(pos).set(true);
						// logger.debug("Longpart " + pos +
						// " receive confirmation: " +
						// Arrays.toString(received));
						for (final AtomicBoolean r : received) {
							if (!r.get())
								return;
						}
						sentListener.received();
					}

					public void sent() {
						sent.get(pos).set(true);
						for (final AtomicBoolean s : sent) {
							if (!s.get())
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

	/**
	 * This repeatedly resends a message until an acknowledgment
	 * is received.
	 * 
	 * @author Ian Clarke <ian.clarke@gmail.com>
	 *
	 */
	private static class Resender implements Runnable {
		/**
		 * This is set to true when an ACK is received in the received() method,
		 * at which point this Resender's work is done
		 */
		public volatile boolean receiptConfirmed = false;
		private final TrSentReceivedListener callbacks;
		private final double initialPriority;
		private final int maxRetries;
		private final ByteArraySegment message;
		private final int messageId;
		private final UdpRemoteConnection parent;
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
			// If it's time to give up, give up
			if (retryCount == maxRetries || receiptConfirmed || parent.shutdown) {
				parent.resenders.remove(messageId);
				if (retryCount == maxRetries || parent.shutdown) {
					callbacks.failure();
				}
			} else {
				// Otherwise, (re)send the message
				parent.iface.sendTo(parent.remoteAddress, message, new TrSentListener() {

					public void failure() {
						// TODO: Should probably complain or something
					}

					public void sent() {
						if (thisRetryNo == 0) {
							callbacks.sent();
						}
						// And schedule sending the next message in case this one doesn't work
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
