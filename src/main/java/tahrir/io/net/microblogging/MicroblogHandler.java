package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.*;

import tahrir.*;
import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.tools.TrUtils;

import com.google.inject.internal.Sets;

/**
 * Handles scheduling of a microblog broadcast and storing of microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogHandler {
	private final Logger logger = LoggerFactory.getLogger(MicroblogBroadcastSessionImpl.class.getName());

	private final MicroblogContainer mbQueue = new MicroblogContainer();

	private TrNode node;

	private Microblog currentlyBroadcasting;
	private Iterator<PhysicalNetworkLocation> peerIter;

	public MicroblogHandler(final TrNode node) {
		if (node.config.peers.runBroadcast) {
			this.node = node;
			scheduleForLater();
		}
	}

	protected void setupForNextMicroblog() {
		final Map<PhysicalNetworkLocation, TrPeerInfo> peerMap = node.peerManager.peers;

		// we don't want to iterate over any new peers added as we could be there forever
		// so we take a snap shot of the set
		final Set<PhysicalNetworkLocation> snapShot = new HashSet<PhysicalNetworkLocation>();

		for (final TrPeerInfo peerInfo : peerMap.values()) {
			if (peerInfo.capabilities.receivesMessageBroadcasts) {
				snapShot.add(peerInfo.remoteNodeAddress.physicalLocation);
			}
		}

		// don't want to clog up network if we don't have minimum peers
		if (snapShot.size() >= node.peerManager.config.minPeers) {
			peerIter = snapShot.iterator();

			currentlyBroadcasting = mbQueue.getMicroblogForBroadcast();

			startBroadcastToPeer();
		} else {
			scheduleForLater();
		}
	}

	protected void startBroadcastToPeer() {
		if (peerIter.hasNext()) {
			final PhysicalNetworkLocation currentPeer = peerIter.next();
			final MicroblogBroadcastSessionImpl localBroadcastSess = node.sessionMgr.getOrCreateLocalSession(MicroblogBroadcastSessionImpl.class);
			localBroadcastSess.startSingleBroadcast(currentlyBroadcasting, currentPeer);
		} else {
			setupForNextMicroblog();
		}
	}

	public MicroblogContainer getMbQueue() {
		return mbQueue;
	}

	private void scheduleForLater() {
		TrUtils.executor.schedule(new RunBroadcast(), 1, TimeUnit.MINUTES);
	}

	private class RunBroadcast implements Runnable {
		public void run() {
			setupForNextMicroblog();
		}
	}

	public static class MicroblogContainer {
		private final PriorityQueue<Microblog> microblogBroadcastQueue = new PriorityQueue<Microblog>(11, new MicroblogPriorityComparator());
		private final Set<Integer> seen = Sets.newLinkedHashSet();

		private final SortedSet<Microblog> microblogsForViewing = Collections.synchronizedSortedSet(new TreeSet<Microblog>(new MicroblogViewingComparator()));

		public void insert(final Microblog mb) {
			// this check probably isn't necessary but just to be sure...
			if (!microblogBroadcastQueue.contains(mb)) {
				microblogBroadcastQueue.add(mb);
				seen.add(mb.hashCode());
			}
			if (isMicroblogForViewing()) {
				microblogsForViewing.add(mb);
			}
		}

		public Microblog getMicroblogForBroadcast() {
			return microblogBroadcastQueue.poll();
		}

		public void changePriority(final Microblog mb, final int priority) {
			microblogBroadcastQueue.remove(mb);
			mb.priority = priority;
			microblogBroadcastQueue.add(mb);
		}

		public boolean isLikelyToContain(final int microblogHash) {
			return seen.contains(microblogHash);
		}

		public boolean contains(final Microblog mb) {
			return microblogBroadcastQueue.contains(mb);
		}

		/*
		 * TODO: Checks if the user is going to want to read the message if they do
		 * want to do read it might be a good idea to reset the priority
		 */
		private boolean isMicroblogForViewing() {
			return true;
		}
	}

	public static class Microblog {
		public int priority;
		private TrSignature signature;
		private String languageCode;
		private String authorNick;
		private RSAPublicKey publicKey;
		public String message;
		public long timeCreated;

		// for serialization
		public Microblog() {

		}

		public Microblog(final TrNode creatingNode, final String message) throws Exception {
			this(creatingNode, message, TrConstants.BROADCAST_INIT_PRIORITY);
		}

		// messy to have constructor throwing exception?
		public Microblog(final TrNode creatingNode, final String message, final int priority) throws Exception {
			this.priority = priority;
			timeCreated = System.currentTimeMillis();
			this.message = message;
			languageCode = ""; // TODO: get language code from config?
			authorNick = ""; // TODO: get nick from config?
			publicKey = creatingNode.getRemoteNodeAddress().publicKey;
			signature = TrCrypto.sign(message, creatingNode.getPrivateNodeId().privateKey);
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

	private static class MicroblogPriorityComparator implements Comparator<Microblog> {
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.priority, mb2.priority);
		}
	}

	private static class MicroblogViewingComparator implements Comparator<Microblog> {
		// not sure how good this is a way of comparing them, we'll have to wait to the GUI
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.timeCreated, mb2.timeCreated);
		}
	}
}
