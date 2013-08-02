package tahrir.tools;

import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import tahrir.TrNodeConfig;
import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.tools.GsonSerializers.RSAPublicKeyDeserializer;
import tahrir.tools.GsonSerializers.RSAPublicKeySerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TrUtils {
	public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

	public static final Random rand = new Random();
	public static final Runnable noopRunnable = new Runnable() {
		public void run() {
			// nothing
		}
	};

	public static final Gson gson;

	public static final EventBus eventBus = new EventBus();

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(RSAPublicKey.class, new RSAPublicKeySerializer());
		builder.registerTypeAdapter(RSAPublicKey.class, new RSAPublicKeyDeserializer());
		gson = builder.create();
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseJson(final File jsonFile, final Type type) throws JsonParseException, IOException {
		final FileReader json = new FileReader(jsonFile);
		T ret = (T) gson.<Object>fromJson(json, type);
        json.close();
        return ret;
	}

	public static <T> T parseJson(final String json, final Class<T> type) throws JsonParseException {
		return gson.fromJson(json, type);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseJson(final String json, final Type type) throws JsonParseException {
		return (T) gson.<Object>fromJson(json, type);
	}

	public static <T> void writeJson(T objectToWrite, File writeTo) throws IOException {
		final FileWriter configWriter = new FileWriter(writeTo);
		configWriter.append(TrUtils.gson.toJson(objectToWrite));
		configWriter.close();
	}

	public static void readAllBytes(final byte[] buffer, final DataInputStream dis) throws IOException {
		int read = 0;
		int numRead = 0;
		while (read < buffer.length && (numRead = dis.read(buffer, read, buffer.length - read)) >= 0) {
			read = read + numRead;
		}
	}

	/**
	 * Utility methods for testing purposes.
	 */
	public static class TestUtils {
		public static boolean isConnected(final TrNode node1, final TrNode node2) {
			return node1.peerManager.peers.containsKey(node2.getRemoteNodeAddress().physicalLocation)
					&& node2.peerManager.peers.containsKey(node1.getRemoteNodeAddress().physicalLocation);
		}

		public static TrNode makeNode(final int port, final boolean maintenance, final boolean assimilate,
				final boolean topologyMaintenace, final boolean broadcast,
				final int minPeers, final int maxPeers) throws Exception {
			final File nodeDir = createTempDirectory();

			final TrNodeConfig nodeConfig = new TrNodeConfig();

			nodeConfig.udp.listenPort = port;
			nodeConfig.localHostName = "127.0.0.1";
			nodeConfig.peers.runMaintainance = maintenance;
			nodeConfig.peers.assimilate = assimilate;
			nodeConfig.peers.topologyMaintenance = topologyMaintenace;
			nodeConfig.peers.runBroadcast = broadcast;
			nodeConfig.peers.minPeers = minPeers;
			nodeConfig.peers.maxPeers = maxPeers;

			final File joinerPubNodeIdsDir = new File(nodeDir, nodeConfig.publicNodeIdsDir);

			joinerPubNodeIdsDir.mkdir();

			return new TrNode(nodeDir, nodeConfig);
		}

		public static void createBidirectionalConnection(final TrNode node1, final TrNode node2) {
			node1.peerManager.addNewPeer(node2.getRemoteNodeAddress(), node2.config.capabilities,
					node2.peerManager.locInfo.getLocation());
			node2.peerManager.addNewPeer(node1.getRemoteNodeAddress(), node1.config.capabilities,
					node1.peerManager.locInfo.getLocation());
		}

		/**
		 * Get a microblog by a random user which has a mention to another random user.
		 */
		public static BroadcastMessage getBroadcastMessage() {
			int mbPosition = 0;
			ParsedPart text = new TextPart(mbPosition++, "Here's a mention of a random user ");
			ParsedPart mention = new MentionPart(mbPosition++, TrCrypto.createRsaKeyPair().a, "anAlias");

			SortedMultiset<ParsedPart> parsedParts = TreeMultiset.create(new PositionComparator());
			parsedParts.add(text);
			parsedParts.add(mention);

			GeneralBroadcastMessageInfo mbData = new GeneralBroadcastMessageInfo(null, "aAuthor", TrCrypto.createRsaKeyPair().a,
					System.currentTimeMillis());
			return new ParsedBroadcastMessage(mbData, ImmutableSortedMultiset.copyOf(new PositionComparator(),
					parsedParts));
		}

		/**
		 * Get a microblog from a user that mentions another user twice.
		 */
		public static ParsedBroadcastMessage getParsedMicroblog(UserIdentity from, UserIdentity mention) {
			int mbPosition = 0;
			ParsedPart mentionPart = new MentionPart(mbPosition++, mention.getPubKey(), mention.getNick());
			ParsedPart textPart = new TextPart(mbPosition++, " was just mentioned.");
			ParsedPart anotherMentionPart = new MentionPart(mbPosition++, mention.getPubKey(), mention.getNick());
			ParsedPart anotherTextPart = new TextPart(mbPosition++, " and look he was just mentioned again!");

			SortedMultiset<ParsedPart> parsedParts = TreeMultiset.create(new PositionComparator());
			parsedParts.add(mentionPart);
			parsedParts.add(textPart);
			parsedParts.add(anotherMentionPart);
			parsedParts.add(anotherTextPart);

			GeneralBroadcastMessageInfo mbData = new GeneralBroadcastMessageInfo(null, from.getNick(), from.getPubKey(), System.currentTimeMillis());
			return new ParsedBroadcastMessage(mbData, ImmutableSortedMultiset.copyOf(new PositionComparator(), parsedParts));
		}

		/**
		 * Get microblog from a user which is just text, no mentions.
		 */
		public static ParsedBroadcastMessage getParsedMicroblog(UserIdentity from) {
			int mbPosition = 0;
			ParsedPart textPart = new TextPart(mbPosition++, "This is just a plain text microblog.");

			SortedMultiset<ParsedPart> parsedParts = TreeMultiset.create(new PositionComparator());
			parsedParts.add(textPart);

			GeneralBroadcastMessageInfo mbData = new GeneralBroadcastMessageInfo(null, from.getNick(), from.getPubKey(), System.currentTimeMillis());
			return new ParsedBroadcastMessage(mbData, ImmutableSortedMultiset.copyOf(new PositionComparator(), parsedParts));
		}

		public static File createTempDirectory() throws IOException {
			final File temp;

			temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

			if (!(temp.delete()))
				throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());

			if (!(temp.mkdir()))
				throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());

			return (temp);
		}
	}
}
