package tahrir.peerManager;

import java.security.PublicKey;
import java.util.Map;

import tahrir.io.net.*;
import tahrir.tools.TrUtils;

import com.google.common.collect.MapMaker;

public class TrPeerManager {
	public Map<TrRemoteAddress, TrPeerInfo> peers = new MapMaker().makeMap();
	public final Config config;

	public final int uid;
	private final TrNet trNet;

	public TrPeerManager(final Config config, final TrNet trNet) {
		this.config = config;
		this.trNet = trNet;
		uid = TrUtils.rand.nextInt();
	}

	public static class Capabilities {
		public boolean allowsUnsolicitiedInbound;
		public boolean allowsAssimilation;
		public boolean receivesMessageBroadcasts;
	}

	public static class TrPeerInfo {
		public PublicKey publicKey;
		public Capabilities capabilities;

		public class assimilation {
			public Stat successRate;
			public Stat time;
		}
	}

	public boolean shouldRetainConnectionTo(final TrRemoteAddress ra) {
		return peers.containsKey(ra);
	}

	public static class Config {
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
}
