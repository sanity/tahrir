package tahrir;

import java.io.File;
import java.net.*;
import java.security.interfaces.*;
import java.util.Collection;

import org.slf4j.*;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.TrRemoteAddress;
import tahrir.io.net.udpV1.UdpRemoteAddress;
import tahrir.peerManager.TrPeerManager;
import tahrir.tools.*;
import tahrir.tools.Persistence.Modified;
import tahrir.tools.Persistence.ModifyBlock;

import com.google.inject.internal.Lists;


/**
 * The root class for the internal state of this node
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 */
public class TrNode {

	Logger logger = LoggerFactory.getLogger(TrNode.class);

	public final TrConfig config;

	private File privNodeIdFile;

	private File pubNodeIdFile;

	private File publicNodeIdsDir;

	public TrNode(final TrPeerManager peerManager, final File rootDirectory, final TrConfig config) {
		this.peerManager = peerManager;
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
								config.udpListenPort);
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
	}

	// For unit tests
	public TrNode() {
		logger.warn("Creating empty TrNode, only do this for unit tests");
		config = null;
		peerManager = null;
		rootDirectory = null;
	}

	public Collection<File> getPublicNodeIdFiles() {
		return Lists.newArrayList(publicNodeIdsDir.listFiles());
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

	public static class PublicNodeIdStats {
		public PublicNodeId id;
		public int connectionAttempts = 0;
		public int connectionFailures = 0;
	}

	public static class PublicNodeId {
		public TrRemoteAddress address;
		public RSAPublicKey publicKey;

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
