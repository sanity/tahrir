package tahrir;

import java.security.PublicKey;
import java.util.Map;

import tahrir.io.net.TrRemoteAddress;

import com.google.common.collect.MapMaker;

public class TrPeerManager {
	public Map<TrRemoteAddress, TrPeerInfo> peers = new MapMaker().makeMap();
	public final Config config;

	public TrPeerManager(final Config config) {
		this.config = config;
	}

	public static class TrPeerInfo {
		public PublicKey publicKey;
		public boolean allowsUnsolicited;

		public class assimilation {
			public Stat successRate;
			public Stat time;
		}
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
