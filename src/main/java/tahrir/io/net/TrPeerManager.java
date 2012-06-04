package tahrir.io.net;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import net.sf.doodleproject.numerics4j.random.BetaRandomVariable;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.net.TrPeerManager.TrPeerInfo.Assimilation;
import tahrir.io.net.sessions.*;
import tahrir.tools.*;
import tahrir.tools.Persistence.Modified;

public class TrPeerManager {
	public static final double RECENTLY_ATTEMPTED_PENALTY = 1.3;

	static Logger logger = LoggerFactory.getLogger(TrPeerManager.class);
	public final Config config;

	public ConcurrentLinkedQueue<TrPeerInfo> lastAttemptedRelays = new ConcurrentLinkedQueue<TrPeerInfo>();

	public Map<PhysicalNetworkLocation, TrPeerInfo> peers = new MapMaker().makeMap();
	public final String sessionMgrLabel;

	private final TrNode node;

	public TrPeerManager(final Config config, final TrNode node) {
		this.config = config;
		this.node = node;
		sessionMgrLabel = "TrPeerManager(" + TrUtils.rand.nextInt() + ")";
		if (config.runMaintainance ) {
			TrUtils.executor.scheduleAtFixedRate(new Runnable() {

				public void run() {
					try {
						maintainance();
					} catch (final Exception e) {
						logger.error("Error running maintainance", e);
					}
				}
			}, 0, 1, TimeUnit.MINUTES);
		}
	}

	public void addNewPeer(final RemoteNodeAddress pubNodeAddress, final Capabilities capabilities) {
		logger.debug("addNewPeer "+pubNodeAddress);
		final TrPeerInfo tpi = new TrPeerInfo(pubNodeAddress);
		tpi.capabilities = capabilities;
		peers.put(pubNodeAddress.location, tpi);
		node.sessionMgr.connectionManager.getConnection(pubNodeAddress, false, sessionMgrLabel, new Runnable() {

			public void run() {
				peers.remove(pubNodeAddress.location);
			}
		});
	}

	public void addByReplacement(final RemoteNodeAddress pubNodeAddress, final Capabilities capabilities) {
		if (peers.size() < config.maxPeers) {
			// just add it regularly
			addNewPeer(pubNodeAddress, capabilities);
		} else {
			// add it by replacement removing LRU peer
			final PhysicalNetworkLocation toRemove = getLeastRecentlyUsedPeer();
			peers.remove(toRemove);
			addNewPeer(pubNodeAddress, capabilities);
		}
	}

	public TrPeerInfo getPeerForAssimilation() {
		if (peers.isEmpty()) {
			// We need to use a public peer
			final ArrayList<File> publicNodeIdFiles = node.getPublicNodeIdFiles();
			final File pubPeerFile = publicNodeIdFiles.get(TrUtils.rand.nextInt(publicNodeIdFiles.size()));
			final TrPeerInfo pnii = Persistence.loadReadOnly(TrPeerInfo.class, pubPeerFile);
			return pnii;
		} else {
			/**
			 * Here we use a trick to pick peers in proportion to the
			 * probability that they will be the fastest peer
			 */
			TrPeerInfo bestPeer = null;
			double bestTimeEstimate = Double.MAX_VALUE;
			final LinearStat globalSuccessTime = new LinearStat(Integer.MAX_VALUE);
			globalSuccessTime.sample(1000);
			globalSuccessTime.sample(2000);
			for (final TrPeerInfo ifo : peers.values()) {
				if (ifo.assimilation.successTimeSqrt.total > 0) {
					globalSuccessTime.sample(ifo.assimilation.successTimeSqrt.mean());
				}
			}
			for (final Entry<PhysicalNetworkLocation, TrPeerInfo> e : peers.entrySet()) {
				final double guessFailureProb = e.getValue().assimilation.successRate.getBetaRandom();
				double guessSuccessTime;
				// If we don't have at least 2 samples, use our global success
				// time
				if (e.getValue().assimilation.successTimeSqrt.total > 2) {
					final double guessSuccessTimeSqrt = e.getValue().assimilation.successTimeSqrt.getNormalRandom();
					guessSuccessTime = guessSuccessTimeSqrt * guessSuccessTimeSqrt;
				} else {
					final double guessSuccessTimeSqrt = globalSuccessTime.getNormalRandom();
					guessSuccessTime = guessSuccessTimeSqrt * guessSuccessTimeSqrt;
				}
				double timeEstimate = guessSuccessTime + AssimilateSessionImpl.RELAY_ASSIMILATION_TIMEOUT_SECONDS
						* 1000l * guessFailureProb;

				if (lastAttemptedRelays.contains(e.getValue())) {
					timeEstimate *= RECENTLY_ATTEMPTED_PENALTY;
				}

				if (timeEstimate < bestTimeEstimate) {
					bestPeer = e.getValue();
					bestTimeEstimate = timeEstimate;
				}
			}
			lastAttemptedRelays.add(bestPeer);
			while (lastAttemptedRelays.size() > 5) {
				lastAttemptedRelays.poll();
			}
			return bestPeer;
		}
	}

	public void maintainance() {
		// Check to see whether we need new connections

		// TODO: We might want to spawn multiple assimilation requests (say, up to 3)
		// in the event that we are well below minPeers to speed this up
		if (config.assimilate && peers.size() < config.minPeers) {
			final AssimilateSessionImpl as = node.sessionMgr.getOrCreateLocalSession(AssimilateSessionImpl.class);
			final TrPeerInfo ap = getPeerForAssimilation();

			as.startAssimilation(TrUtils.noopRunnable, ap);

			// } else {
			// logger.warn("Don't know how to assimilate through already connected peers yet");
			// }
		} else {
			// do maintenance on topology for small world network
			final int randomLocationToFind = Math.abs(TrUtils.rand.nextInt());
			final TopologyMaintenanceSessionImpl tm = node.sessionMgr.getOrCreateLocalSession(TopologyMaintenanceSessionImpl.class);
			tm.startTopologyMaintenance(randomLocationToFind, TrConstants.MAINTENANCE_HOPS_TO_LIVE);
		}
	}

	public void reportAssimilationFailure(final PhysicalNetworkLocation addr) {
		updatePeerInfo(addr, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo peerInfo) {
				final Assimilation a = peerInfo.assimilation;
				a.successRate.sample(false);
				a.lastFailureTime = System.currentTimeMillis();
				// If we've tried it three times, and it failed more than half
				// the time, let's get rid of it
				if (a.successRate.total > 3 && a.successRate.get() < 0.5) {
					node.sessionMgr.connectionManager.noLongerNeeded(addr, sessionMgrLabel);
				}
				return null;
			}
		});
	}

	public void reportAssimilationSuccess(final PhysicalNetworkLocation addr, final long timeMS) {
		updatePeerInfo(addr, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo peerInfo) {
				final Assimilation a = peerInfo.assimilation;
				a.successRate.sample(true);
				a.successTimeSqrt.sample(Math.sqrt(timeMS));
				return null;
			}
		});
	}

	public RemoteNodeAddress getClosestPeer(final int locationToFind) {
		// closest peer is initially calling node
		RemoteNodeAddress closestPeer = node.getRemoteNodeAddress();
		final int callingNodeTopologyLoc = TopologyMaintenanceSessionImpl.calcTopologyLoc(closestPeer.publicKey);
		int closestDistance = findDistanceWithRollover(callingNodeTopologyLoc, locationToFind);

		for (final TrPeerInfo ifo : peers.values()) {
			final int currentPeerDistance = findDistanceWithRollover(ifo.topologyLocation, locationToFind);

			if (currentPeerDistance < closestDistance) {
				closestDistance = currentPeerDistance;
				closestPeer  = ifo.remoteNodeAddress;
			}
		}

		return closestPeer;
	}

	public void updateTimeLastUsed(final PhysicalNetworkLocation physicalLocation) {
		// TODO: is this the correct way to use this?
		updatePeerInfo(physicalLocation, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo peerInfo) {
				peerInfo.lastTimeUsed = System.currentTimeMillis();
				return null;
			}
		});
	}

	public void enableDebugMaintenance() {
		// run maintenance more often
		TrUtils.executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				try {
					maintainance();
				} catch (final Exception e) {
					logger.error("Error running maintainance", e);
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
		// allow topology probing more often
		TopologyMaintenanceSessionImpl.enableDebugProbing();
	}

	public int getNumFreePeerSlots() {
		return config.maxPeers - peers.size();
	}

	private int findDistanceWithRollover(final int from, final int to) {
		return Math.abs((from % Integer.MAX_VALUE) - to);
	}

	private PhysicalNetworkLocation getLeastRecentlyUsedPeer() {
		PhysicalNetworkLocation leastRecentlyUsedPeer = null;
		long longestTimeSinceUsed = Long.MIN_VALUE;

		for (final TrPeerInfo ifo : peers.values()) {
			if (ifo.lastTimeUsed > longestTimeSinceUsed) {
				leastRecentlyUsedPeer = ifo.remoteNodeAddress.location;
				longestTimeSinceUsed = ifo.lastTimeUsed;
			}
		}

		return leastRecentlyUsedPeer;
	}

	/**
	 * If you need to modify a peer's information you must do it using this
	 * method, as it ensures that persistent peer information gets persisted
	 * 
	 * @param addr
	 * @param updateFunction
	 */
	public void updatePeerInfo(final PhysicalNetworkLocation addr, final Function<TrPeerInfo, Void> updateFunction) {
		final File pubNodeFile = node.getFileForPublicNode(addr);
		if (pubNodeFile.exists()) {
			Persistence.loadAndModify(TrPeerInfo.class, pubNodeFile, new Persistence.ModifyBlock<TrPeerInfo>() {

				public void run(final TrPeerInfo object, final Modified modified) {
					updateFunction.apply(object);
				}
			});
		} else {
			final TrPeerInfo peerToUpdate = peers.get(addr);
			if (peerToUpdate != null) {
				updateFunction.apply(peerToUpdate);
			} else {
				logger.warn("Attempted to update unknown peer "+addr+", ignoring");
			}
		}
	}

	public static class BinaryStat {
		private int maxRecall;
		private long sum;
		private long total;

		// For serialization
		public BinaryStat() {

		}

		public BinaryStat(final int maxRecall) {
			this.maxRecall = maxRecall;
			total = 0;
			sum = 0;
		}

		public double get() {
			return sum / total;
		}

		public double getBetaRandom() {
			return BetaRandomVariable.nextRandomVariable(1 + sum, 1 + total - sum, TrUtils.rng);
		}

		public void sample(final boolean value) {
			total++;
			if (value) {
				sum++;
			}
			if (total >= maxRecall) {
				total = total / 2;
				sum = sum / 2;
			}
		}
	}

	public static class Capabilities {
		public boolean allowsAssimilation;
		public boolean allowsUnsolicitiedInbound;
		public boolean receivesMessageBroadcasts;

		@Override
		public String toString() {
			return "Capabilities [allowsAssimilation=" + allowsAssimilation + ", allowsUnsolicitiedInbound="
					+ allowsUnsolicitiedInbound + ", receivesMessageBroadcasts=" + receivesMessageBroadcasts + "]";
		}
	}

	public static class Config {
		public boolean runMaintainance = true;
		public boolean assimilate = true;
		public int maxPeers = 20;
		public int minPeers = 10;
	}

	public static final class LinearStat {
		private int maxRecall;
		private double sum, sq_sum;
		private long total;

		// For serialization
		public LinearStat() {

		}

		public LinearStat(final int maxRecall) {
			this.maxRecall = maxRecall;
			total = 0;
			sum = 0;
			sq_sum = 0;
		}

		public double getNormalRandom() {
			return TrUtils.rand.nextGaussian() * getStandardDeviation() + mean();
		}

		public double getStandardDeviation() {
			return Math.sqrt(sq_sum / total - (mean() * mean()));
		}

		public double mean() {
			return sum / total;
		}

		public void sample(final double value) {
			total++;
			sum += value;
			sq_sum += value * value;

			if (total >= maxRecall) {
				total = total / 2;
				sum = sum / 2;
				sq_sum = sq_sum / 2;
			}
		}

	}

	public static class TrPeerInfo {
		public Assimilation assimilation = new Assimilation();
		public Capabilities capabilities;
		public RemoteNodeAddress remoteNodeAddress;
		public int topologyLocation;
		public long lastTimeUsed;

		// To allow deserialization
		public TrPeerInfo() {

		}

		public TrPeerInfo(final RemoteNodeAddress remoteNodeAddress) {
			this.remoteNodeAddress = remoteNodeAddress;
			topologyLocation = TopologyMaintenanceSessionImpl.calcTopologyLoc(remoteNodeAddress.publicKey);
		}

		public static class Assimilation {
			public long lastFailureTime = 0;
			public BinaryStat successRate = new BinaryStat(10);
			public LinearStat successTimeSqrt = new LinearStat(10);
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("TrPeerInfo [remoteNodeAddress=");
			builder.append(remoteNodeAddress);
			builder.append("]");
			return builder.toString();
		}
	}
}