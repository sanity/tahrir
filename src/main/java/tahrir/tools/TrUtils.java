package tahrir.tools;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import tahrir.TrNode;
import tahrir.TrNodeConfig;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.broadcasts.UserIdentity;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.SignedBroadcastMessage;
import tahrir.tools.GsonSerializers.RSAPublicKeyDeserializer;
import tahrir.tools.GsonSerializers.RSAPublicKeySerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.security.interfaces.RSAPrivateKey;
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
        builder.registerTypeAdapterFactory(new OptionalTypeAdapterFactory());
		builder.registerTypeAdapter(RSAPublicKey.class, new RSAPublicKeySerializer());
		builder.registerTypeAdapter(RSAPublicKey.class, new RSAPublicKeyDeserializer());
        builder.registerTypeAdapter(RSAPrivateKey.class, new GsonSerializers.RSAPrivateKeySerializer());
        builder.registerTypeAdapter(RSAPrivateKey.class, new GsonSerializers.RSAPrivateKeyDeserializer());
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
			return node1.getPeerManager().peers.containsKey(node2.getRemoteNodeAddress().physicalLocation)
					&& node2.getPeerManager().peers.containsKey(node1.getRemoteNodeAddress().physicalLocation);
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
			node1.getPeerManager().addNewPeer(node2.getRemoteNodeAddress(), node2.getConfig().capabilities,
                    node2.getPeerManager().getLocInfo().getLocation());
			node2.getPeerManager().addNewPeer(node1.getRemoteNodeAddress(), node1.getConfig().capabilities,
                    node1.getPeerManager().getLocInfo().getLocation());
		}

		/**
		 * Get a microblog by a random user which has a mention to another random user.
		 */
		public static BroadcastMessage getBroadcastMessage(TrNode node1) {
            UserIdentity randomUser = new UserIdentity("Random User", TrCrypto.createRsaKeyPair().a, Optional.of(TrCrypto.createRsaKeyPair().b));
            node1.mbClasses.identityStore.addIdentity(randomUser);
			ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("Hi @User3, How are you?", "en", node1.mbClasses.identityStore, System.currentTimeMillis());
            SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, randomUser);
            BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
	        return broadcastMessage;
		}

		/**
		 * Get a microblog from a user that mentions another user twice.
		 */
		public static BroadcastMessage getBroadcastMessage(UserIdentity from, UserIdentity mention, TrNode node) {
            ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("Hi @"+mention.getNick()+", this is a sample with mention", "en", node.mbClasses.identityStore, System.currentTimeMillis());
            SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, from);
            BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
            return broadcastMessage;
		}

		/**
		 * Get broadcastMessage from a user which is just text, no mentions.
		 */
		public static BroadcastMessage getBroadcastMessageFrom(TrNode node) {
            ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("Some post from a user.", "en", node.mbClasses.identityStore, System.currentTimeMillis());
            SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, node.getConfig().currentUserIdentity);
            BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
            return broadcastMessage;

		}

        public static BroadcastMessage getBroadcastMessageFrom(TrNode node, UserIdentity identity) {
            ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext("From a given node, given user.", "en", node.mbClasses.identityStore, System.currentTimeMillis());
            SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, identity);
            BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
            return broadcastMessage;

        }

		public static File createTempDirectory() throws IOException {
			final File temp;

			temp = Files.createTempDirectory("temp" + Long.toString(System.nanoTime())).toFile();

			return (temp);
		}
	}
}
