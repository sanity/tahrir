package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.crypto.*;
import tahrir.io.net.PhysicalNetworkLocation;
import tahrir.tools.TrUtils;

import com.google.inject.internal.Sets;

/**
 * Handles scheduling of a microblog broadcast and storing of microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogHandler {
	private final Logger logger = LoggerFactory.getLogger(MicroblogBroadcastSessionImpl.class.getName());

	private final MicroblogQueue mbQueue = new MicroblogQueue();

	private final TrNode node;

	private Microblog currentlyBroadcasting;
	private Iterator<PhysicalNetworkLocation> peerIter;

	public MicroblogHandler(final TrNode node) {
		this.node = node;
		scheduleForLater();
	}

	private void setUpForBroadcast() {
		if (node.peerManager.peers.size() > node.peerManager.config.minPeers) {
			// we don't want to iterate over any new peers added as we could be there forever
			// so we take a snap shot of the set
			final Set<PhysicalNetworkLocation> snapShot = new TreeSet<PhysicalNetworkLocation>();
			node.peerManager.peers.keySet().addAll(snapShot);
			peerIter = snapShot.iterator();

			currentlyBroadcasting = mbQueue.getMicroblogForBroadcast();

			start();
		} else {
			scheduleForLater();
		}
	}

	public void start() {
		startNext();
	}

	public void startNext() {
		if (peerIter.hasNext()) {
			final PhysicalNetworkLocation currentPeer = peerIter.next();
			final MicroblogBroadcastSessionImpl localBroadcastSess = node.sessionMgr.getOrCreateLocalSession(MicroblogBroadcastSessionImpl.class);
			localBroadcastSess.startSingleBroadcast(currentlyBroadcasting, currentPeer);
		} else {
			setUpForBroadcast();
		}
	}

	public MicroblogQueue getMbQueue() {
		return mbQueue;
	}

	private void scheduleForLater() {
		TrUtils.executor.schedule(new RunBroadcast(), 1, TimeUnit.MINUTES);
	}

	private class RunBroadcast implements Runnable {
		public void run() {
			setUpForBroadcast();
		}
	}

	public static class MicroblogQueue {
		private final PriorityQueue<Microblog> microBlogs = new PriorityQueue<Microblog>();
		private final Set<Integer> seen = Sets.newLinkedHashSet();

		public void insert(final Microblog mb) {
			// this check probably isn't necessary but just to be sure...
			if (!microBlogs.contains(mb)) {
				microBlogs.add(mb);
				seen.add(mb.hashCode());
			}
		}

		public Microblog getMicroblogForBroadcast() {
			final Microblog mb =  microBlogs.poll();
			if (mb != null) {
				mb.priority++;
				microBlogs.add(mb);
			}
			return mb;
		}

		public void changePriority(final Microblog mb, final int priority) {
			microBlogs.remove(mb);
			mb.priority = priority;
			microBlogs.add(mb);
		}

		public boolean isLikelyToContain(final int microblogHash) {
			return seen.contains(microblogHash);
		}
	}

	public static class Microblog implements Comparable<Microblog> {
		public int priority;
		private final TrSignature signature;
		private final String languageCode;
		private final String authorNick;
		private final RSAPublicKey publicKey;
		private final String message;
		private final long timeCreated;

		// messy to have constructor throwing exception?
		public Microblog (final TrNode creatingNode, final String message) throws Exception {
			priority = TrConstants.BROADCAST_INIT_TSU;
			timeCreated = System.currentTimeMillis();
			this.message = message;
			languageCode = ""; // TODO: get language code from config?
			authorNick = ""; // TODO: get nick from config?
			publicKey = creatingNode.getRemoteNodeAddress().publicKey;
			signature = TrCrypto.sign(message, creatingNode.getPrivateNodeId().privateKey);
		}

		@Override
		public int compareTo(final Microblog mb) {
			// a lower TSU should be greater
			return -1 * Double.compare(priority, mb.priority);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((signature == null) ? 0 : signature.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Microblog other = (Microblog) obj;
			if (signature == null) {
				if (other.signature != null)
					return false;
			} else if (!signature.equals(other.signature))
				return false;
			return true;
		}
	}
}
