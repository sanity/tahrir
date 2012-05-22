package tahrir;

import java.io.File;
import java.net.*;
import java.security.interfaces.*;
import java.util.ArrayList;

import org.slf4j.*;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.*;
import tahrir.io.net.sessions.*;
import tahrir.io.net.udpV1.*;
import tahrir.tools.*;
import tahrir.tools.Persistence.Modified;
import tahrir.tools.Persistence.ModifyBlock;

import com.google.common.collect.Lists;



/**
 * The root class for the internal state of this node
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 */
public class TrNode {

	Logger logger = LoggerFactory.getLogger(TrNode.class);

	public final TrConfig config;

	public File privNodeIdFile;

	public File pubNodeIdFile;

	public File publicNodeIdsDir;

	public TrNode(final File rootDirectory, final TrConfig config)
			throws SocketException {
		this.rootDirectory = rootDirectory;
		this.config = config;
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
						remoteNodeAddress.location = new UdpNetworkLocation(InetAddress.getByName(config.localHostName),
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
	}

	private void registerSessions() {
		sessionMgr.registerSessionClass(AssimilateSession.class, AssimilateSessionImpl.class);
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

	public final TrPeerManager peerManager;

	public final File rootDirectory;

	public TrSessionManager sessionMgr;

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
}
