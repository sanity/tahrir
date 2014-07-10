package tahrir;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.identites.IdentityStore;
import tahrir.identites.UserIdentity;
import tahrir.network.*;
import tahrir.transport.messaging.udpV1.PhysicalNetworkLocation;
import tahrir.transport.rpc.TrNetworkInterface;
import tahrir.transport.rpc.TrPeerManager;
import tahrir.transport.rpc.TrSessionManager;
import tahrir.util.crypto.TrCrypto;
import tahrir.network.broadcasts.*;
import tahrir.network.broadcasts.containers.BroadcastMessageInbox;
import tahrir.network.broadcasts.containers.BroadcastMessageOutbox;
import tahrir.network.sessions.AssimilateSession;
import tahrir.network.sessions.AssimilateSessionImpl;
import tahrir.network.sessions.TopologyMaintenanceSession;
import tahrir.network.sessions.TopologyMaintenanceSessionImpl;
import tahrir.transport.messaging.udpV1.UdpNetworkInterface;
import tahrir.transport.messaging.udpV1.UdpNetworkLocation;
import tahrir.util.tools.Persistence;
import tahrir.util.tools.Persistence.Modified;
import tahrir.util.tools.Persistence.ModifyBlock;
import tahrir.util.tools.TrUtils;
import tahrir.util.tools.Tuple2;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * The root class for the internal state of this node
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 */
public class TrNode {

	Logger logger = LoggerFactory.getLogger(TrNode.class);

	private final TrNodeConfig config;

	public File privNodeIdFile;
	public File pubNodeIdFile;
	public File publicNodeIdsDir;
	public final File rootDirectory;
    public final File identityStoreFile;

	private final TrPeerManager peerManager;

	public MicrobloggingClasses mbClasses;

	public TrSessionManager sessionMgr;


	public TrNode(final File rootDirectory, final TrNodeConfig config)
			throws SocketException {
		this.rootDirectory = rootDirectory;
		this.config = config;
        this.identityStoreFile = new File(rootDirectory+System.getProperty("file.separator")+"id-store.json");

		privNodeIdFile = new File(rootDirectory, config.privateNodeId);
		pubNodeIdFile = new File(rootDirectory, config.publicNodeId);
		if (!privNodeIdFile.exists()) {
			logger.info("Generating new Node ID");
			final Tuple2<PrivateNodeId, RemoteNodeAddress> kp = PrivateNodeId.generate();
			Persistence.save(privNodeIdFile, kp.a);
			Persistence.save(pubNodeIdFile, kp.b);
		}
		if (config.localHostName != null) {
			modifyPublicNodeId(new ModifyBlock<RemoteNodeAddress>() {

				public void run(final RemoteNodeAddress remoteNodeAddress, final Modified modified) {
					try {
						remoteNodeAddress.physicalLocation = new UdpNetworkLocation(InetAddress.getByName(config.localHostName),
								config.udp.listenPort);
					} catch (final UnknownHostException e) {
						logger.error("Failed to set local node address", e);
					}
				}
			});
		}
		publicNodeIdsDir = new File(rootDirectory, config.publicNodeIdsDir);
		if (!publicNodeIdsDir.exists()) {
			publicNodeIdsDir.mkdir();
		}

		logger.info("Set up UDP network interface");
		final Tuple2<RSAPublicKey, RSAPrivateKey> keyPair = Tuple2.of(getRemoteNodeAddress().publicKey,
				getPrivateNodeId().privateKey);
		final TrNetworkInterface uni = new UdpNetworkInterface(config.udp, keyPair);
		sessionMgr = new TrSessionManager(this, uni, config.capabilities.allowsUnsolicitiedInbound);

		logger.info("Set up peer manager");
		peerManager = new TrPeerManager(config.peers, this);

		registerSessions();

		if (config.peers.runBroadcast) {
			mbClasses = new MicrobloggingClasses(this);
		}
	}

	/**
	 * If you want to use a session you must register it with this method.
	 */
	private void registerSessions() {
		sessionMgr.registerSessionClass(TopologyMaintenanceSession.class, TopologyMaintenanceSessionImpl.class);
		sessionMgr.registerSessionClass(AssimilateSession.class, AssimilateSessionImpl.class);
		// don't want to be able to call broadcast methods on a seed node
		if (getConfig().peers.runBroadcast) {
			sessionMgr.registerSessionClass(TransmitMicroblogSession.class, TransmitMicroblogSessionImpl.class);
		}
	}

	public ArrayList<File> getPublicNodeIdFiles() {
		return Lists.newArrayList(publicNodeIdsDir.listFiles());
	}

	public File getFileForPublicNode(final PhysicalNetworkLocation addr) {
		final int hc = Math.abs(addr.hashCode());
		return new File(publicNodeIdsDir, "pn-" + hc + ".dat");
	}

	public PrivateNodeId getPrivateNodeId() {
		return Persistence.loadReadOnly(PrivateNodeId.class, privNodeIdFile);
	}

	public void modifyPrivateNodeId(final ModifyBlock<PrivateNodeId> mb) {
		Persistence.loadAndModify(PrivateNodeId.class, privNodeIdFile, mb);
	}

	public RemoteNodeAddress getRemoteNodeAddress() {
		return Persistence.loadReadOnly(RemoteNodeAddress.class, pubNodeIdFile);
	}

	public void modifyPublicNodeId(final ModifyBlock<RemoteNodeAddress> mb) {
		Persistence.loadAndModify(RemoteNodeAddress.class, pubNodeIdFile, mb);
	}

    public void setCurrentIdentity(String nick){
        for(UserIdentity identity :this.mbClasses.identityStore.getIdentitiesWithNick(nick)){
            if(identity.hasPvtKey() && identity.getNick().equals(nick)){
                this.getConfig().currentUserIdentity = identity;
            }
        }
    }


    public TrNodeConfig getConfig() {
        return config;
    }


    public static class MicrobloggingClasses {
		public final BroadcastMessageBroadcaster mbScheduler;
        public final IdentityStore identityStore;
		public final ShortenedPublicKeyFinder spkFinder;
		public final IncomingBroadcastMessageHandler incomingMbHandler;
		public final BroadcastMessageOutbox mbsForBroadcast;
		public final BroadcastMessageInbox mbsForViewing;
        public final EventBus eventBus= new EventBus();
		public MicrobloggingClasses(final TrNode node) {
            identityStore=new IdentityStore(getOrCreateFile(new File(node.rootDirectory, node.getConfig().contacts)));
            identityStore.setEventBus(eventBus);
			spkFinder = new ShortenedPublicKeyFinder(
					getOrCreateFile(new File(node.rootDirectory, node.getConfig().publicKeyChars)));
			mbsForBroadcast = new BroadcastMessageOutbox();
			mbsForViewing = new BroadcastMessageInbox(identityStore);
			incomingMbHandler = new IncomingBroadcastMessageHandler(mbsForViewing, mbsForBroadcast, identityStore);
			mbScheduler = new BroadcastMessageBroadcaster(node);
            TrUtils.executor.scheduleAtFixedRate(mbScheduler, 1, 1, TimeUnit.MINUTES);
		}

		// move this somewhere else
		private File getOrCreateFile(File f) {
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("Could not create file " + f);
				}
			}
			return f;
		}
	}

	public static class PrivateNodeId {
		public static Tuple2<PrivateNodeId, RemoteNodeAddress> generate() {
			final Tuple2<RSAPublicKey, RSAPrivateKey> kp = TrCrypto.createRsaKeyPair();

			final PrivateNodeId privateNodeId = new PrivateNodeId();
			privateNodeId.privateKey = kp.b;

			final RemoteNodeAddress remoteNodeAddress = new RemoteNodeAddress(null, kp.a);

			return Tuple2.of(privateNodeId, remoteNodeAddress);
		}

		public RSAPrivateKey privateKey;
	}

    public TrPeerManager getPeerManager() {
        return peerManager;
    }
}
