package tahrir.peerManager;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import tahrir.*;
import tahrir.TrNode.PublicNodeIdInfo;
import tahrir.io.net.TrRemoteAddress;
import tahrir.io.net.sessions.AssimilateSessionImpl;
import tahrir.tools.*;
import tahrir.tools.Persistence.Modified;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class TrPeerManager {
	public Map<TrRemoteAddress, TrPeerInfo> peers = new MapMaker().makeMap();
	public final Config config;

	public final String trNetLabel;
	private final TrNode node;

	public TrPeerManager(final Config config, final TrNode node) {
		this.config = config;
		this.node = node;
		trNetLabel = "TrPeerManager(" + TrUtils.rand.nextInt() + ")";
		TrUtils.executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				maintainance();
			}
		}, 0, 1, TimeUnit.MINUTES);
	}

	public void addNewPeer(final TrRemoteAddress address, final RSAPublicKey pubKey, final Capabilities capabilities) {
		final TrPeerInfo tpi = new TrPeerInfo();
		tpi.capabilities = capabilities;
		tpi.publicKey = pubKey;
		peers.put(address, tpi);
		node.trNet.connectionManager.getConnection(address, pubKey, false, trNetLabel, new Runnable() {

			public void run() {
				peers.remove(address);
			}
		});
	}

	public void maintainance() {
		// Check to see whether we need new connections
		if (config.assimilate && peers.size() < config.minPeers) {
			final AssimilateSessionImpl as = node.trNet.getOrCreateLocalSession(AssimilateSessionImpl.class);

			final Entry<TrRemoteAddress, TrPeerInfo> assimilatePeer = getPeerForAssimilation();

			as.startAssimilation(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub

				}
			}, assimilatePeer.getKey(), assimilatePeer.getValue().publicKey,
			assimilatePeer.getValue().capabilities.allowsUnsolicitiedInbound);
		}
	}

	public static class Capabilities {
		public boolean allowsUnsolicitiedInbound;
		public boolean allowsAssimilation;
		public boolean receivesMessageBroadcasts;
	}

	public static class TrPeerInfo {
		public RSAPublicKey publicKey;
		public Capabilities capabilities;
		public Assimilation assimilation = new Assimilation();

		public static class Assimilation {
			public Stat successRate;
			public Stat time;
		}
	}

	public static class Config {
		public boolean assimilate = true;
		public int minPeers = 10;
		public int maxPeers = 20;
	}

	public static class Stat {
		private double total;
		private double sum;

		public Stat() {
			total = 0;
			sum = 0;
		}

		public Stat(final Iterable<Stat> stats) {
			double ptotal = 0, psum = 0;
			for (final Stat s : stats) {
				ptotal++;
				psum += s.get();
			}
			sum = 10;
			total = (psum / ptotal) / 10.0;
		}

		public void sample(final double value) {
			total++;
			sum += value;
		}

		public double get() {
			return sum / total;
		}
	}

	/**
	 * If you need to modify a peer's information you must do it using this
	 * method, as it ensures that persistent peer information gets persisted
	 * 
	 * @param addr
	 * @param updateFunction
	 */
	public void updatePeerInfo(final TrRemoteAddress addr, final Function<TrPeerInfo, Void> updateFunction) {
		final File pubNodeFile = node.getFileForPublicNode(addr);
		if (pubNodeFile.exists()) {
			Persistence.loadAndModify(TrPeerInfo.class, pubNodeFile, new Persistence.ModifyBlock<TrPeerInfo>() {

				public void run(final TrPeerInfo object, final Modified modified) {
					updateFunction.apply(object);
				}
			});
		} else {
			updateFunction.apply(peers.get(addr));
		}
	}

	public TrRemoteAddress getPeerForAssimilation() {
		if (peers.isEmpty()) {
			// We need to use a public peer
			final ArrayList<File> publicNodeIdFiles = node.getPublicNodeIdFiles();
			final File pubPeerFile = publicNodeIdFiles.get(TrUtils.rand.nextInt(publicNodeIdFiles.size()));
			final PublicNodeIdInfo pnii = Persistence.loadReadOnly(PublicNodeIdInfo.class, pubPeerFile);
			return pnii.id.address;
		} else {
			final List<Entry<TrRemoteAddress, TrPeerInfo>> entries = Lists.newArrayList(peers.entrySet());
			// TODO: This should be much smarter, picking the best peer based on
			// stats, avoiding overloading any one peer, etc
			return entries.get(TrUtils.rand.nextInt(entries.size())).getKey();
		}
	}
}
