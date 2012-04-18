package tahrir;

import java.io.File;
import java.net.*;
import java.security.interfaces.*;
import java.util.ArrayList;

import com.google.common.collect.Lists;

import org.slf4j.*;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.*;
import tahrir.io.net.sessions.*;
import tahrir.io.net.udpV1.*;
import tahrir.peerManager.TrPeerManager;
import tahrir.tools.*;
import tahrir.tools.Persistence.Modified;
import tahrir.tools.Persistence.ModifyBlock;



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
			final Tuple2<PrivateNodeId, PublicNodeId> kp = PrivateNodeId.generate();
			Persistence.save(privNodeIdFile, kp.a);
			Persistence.save(pubNodeIdFile, kp.b);
		}
		if (config.localHostName != null) {
			modifyPublicNodeId(new ModifyBlock<TrNode.PublicNodeId>() {

				public void run(final PublicNodeId publicNodeId, final Modified modified) {
					try {
						publicNodeId.address = new UdpRemoteAddress(InetAddress.getByName(config.localHostName),
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
		final Tuple2<RSAPublicKey, RSAPrivateKey> keyPair = Tuple2.of(getPublicNodeId().publicKey,
				getPrivateNodeId().privateKey);
		final TrNetworkInterface uni = new UdpNetworkInterface(config.udp, keyPair);
		trNet = new TrNet(this, uni, config.capabilities.allowsUnsolicitiedInbound);

		logger.info("Set up peer manager");
		peerManager = new TrPeerManager(config.peers, this);

		registerSessions();
	}

	private void registerSessions() {
		trNet.registerSessionClass(AssimilateSession.class, AssimilateSessionImpl.class);
	}

	public ArrayList<File> getPublicNodeIdFiles() {
		return Lists.newArrayList(publicNodeIdsDir.listFiles());
	}

	public File getFileForPublicNode(final TrRemoteAddress addr) {
		final int hc = Math.abs(addr.hashCode());
		return new File(publicNodeIdsDir, "pn-" + hc + ".dat");
	}

	public PrivateNodeId getPrivateNodeId() {
		return Persistence.loadReadOnly(PrivateNodeId.class, privNodeIdFile);
	}

	public void modifyPrivateNodeId(final ModifyBlock<PrivateNodeId> mb) {
		Persistence.loadAndModify(PrivateNodeId.class, privNodeIdFile, mb);
	}

	public PublicNodeId getPublicNodeId() {
		return Persistence.loadReadOnly(PublicNodeId.class, pubNodeIdFile);
	}

	public void modifyPublicNodeId(final ModifyBlock<PublicNodeId> mb) {
		Persistence.loadAndModify(PublicNodeId.class, pubNodeIdFile, mb);
	}

	public final TrPeerManager peerManager;

	public final File rootDirectory;

	public TrNet trNet;

	public static class PrivateNodeId {
		public static Tuple2<PrivateNodeId, PublicNodeId> generate() {
			final PrivateNodeId privateNodeId = new PrivateNodeId();
			final PublicNodeId publicNodeId = new PublicNodeId();

			publicNodeId.address = null;

			final Tuple2<RSAPublicKey, RSAPrivateKey> kp = TrCrypto.createRsaKeyPair();

			publicNodeId.publicKey = kp.a;
			privateNodeId.privateKey = kp.b;

			return Tuple2.of(privateNodeId, publicNodeId);
		}

		public RSAPrivateKey privateKey;
	}

	public static class PublicNodeIdInfo {
		public PublicNodeId id;
		public int connectionAttempts = 0;
		public int connectionFailures = 0;
	}

	public static class PublicNodeId {
		public TrRemoteAddress address;
		public RSAPublicKey publicKey;

		public PublicNodeId() {

		}

		public PublicNodeId(final TrRemoteAddress address,final RSAPublicKey publicKey) {
			this.address = address;
			this.publicKey = publicKey;

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((address == null) ? 0 : address.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PublicNodeId))
				return false;
			final PublicNodeId other = (PublicNodeId) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			return true;
		}
	}
}
