package tahrir.io.net.microblogging;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import tahrir.*;
import tahrir.io.crypto.*;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.tools.TrUtils;

/**
 * Handles scheduling of a microblog broadcast and storing of microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicrobloggingManger {
	private final MicroblogContainer mbContainer = new MicroblogContainer();

	public ContactBook contactBook;
	public DuplicateNameAppender duplicateNameAppender;

	public MicroblogFilter microblogFilter;

	private TrNode node;

	private Microblog currentlyBroadcasting;
	private Iterator<PhysicalNetworkLocation> peerIter;

	public MicrobloggingManger(final TrNode node) {
		if (node.config.peers.runBroadcast) {
			this.node = node;
			scheduleForLater();
			contactBook = new ContactBook(this, new File(node.rootDirectory, node.config.contacts));
			duplicateNameAppender = new DuplicateNameAppender(new File(node.rootDirectory, node.config.publicKeyChars));
			microblogFilter = new MicroblogFilter(mbContainer, contactBook);
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

			currentlyBroadcasting = mbContainer.getMicroblogForBroadcast();

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

	public MicroblogContainer getMicroblogContainer() {
		return mbContainer;
	}

	private void scheduleForLater() {
		TrUtils.executor.schedule(new RunBroadcast(), 1, TimeUnit.MINUTES);
	}

	private class RunBroadcast implements Runnable {
		public void run() {
			setupForNextMicroblog();
		}
	}

	public class MicroblogContainer {
		private final PriorityQueue<Microblog> microblogBroadcastQueue = new PriorityQueue<Microblog>(11, new MicroblogPriorityComparator());
		private final Set<Integer> seen = com.google.common.collect.Sets.newLinkedHashSet();

		private final MicroblogsForViewing microblogsForViewing = new MicroblogsForViewing();

		public Microblog getMicroblogForBroadcast() {
			return microblogBroadcastQueue.poll();
		}

		public void changeBroadcastPriority(final Microblog mb, final int priority) {
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

		public void insert(final Microblog mb) {
			// this check probably isn't necessary but just to be sure...
			if (!microblogBroadcastQueue.contains(mb)) {
				microblogBroadcastQueue.add(mb);
				seen.add(mb.hashCode());
			}
			microblogsForViewing.add(mb);
		}

		public MicroblogsForViewing getMicroblogsForViewing() {
			return microblogsForViewing;
		}
	}

	private class MicroblogsForViewing implements Iterable<Microblog> {
		private final SortedSet<Microblog> microblogsViewingSet = Collections.synchronizedSortedSet(new TreeSet<Microblog>(new MicroblogViewingComparator()));

		private void add(final Microblog mb) {
			// TODO: may want to increase priority if added
			if (microblogsViewingSet.size() < TrConstants.MAX_MICROBLOGS_FOR_VIEWING) {
				microblogsViewingSet.add(mb);
			} else if (shouldAddByReplacement(mb)) {
				microblogsViewingSet.remove(microblogsViewingSet.last());
				microblogsViewingSet.add(mb);
			}
		}

		// TODO: probably needs to check more things
		private boolean shouldAddByReplacement(final Microblog mb) {
			if (contactBook.contactsContainer.getContact(mb.publicKey) != null)
				return true;
			else
				return false;
		}

		@Override
		public Iterator<Microblog> iterator() {
			return microblogsViewingSet.iterator();
		}
	}

	public static class Microblog {
		public int priority;
		public TrSignature signature;
		public String languageCode;
		public String authorNick;
		public RSAPublicKey publicKey;
		public String message;
		public long timeCreated;

		// for serialization
		public Microblog() {

		}

		public Microblog(final TrNode creatingNode, final String message) {
			this(creatingNode, message, TrConstants.BROADCAST_INIT_PRIORITY);
		}

		public Microblog(final TrNode creatingNode, final String message, final int priority) {
			this.priority = priority;
			timeCreated = System.currentTimeMillis();
			this.message = message;
			languageCode = ""; // TODO: get language code from config?
			authorNick = ""; // TODO: get nick from config?
			publicKey = creatingNode.getRemoteNodeAddress().publicKey;
			try {
				signature = TrCrypto.sign(message, creatingNode.getPrivateNodeId().privateKey);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
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
		// not sure how good this is a way of comparing them
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.timeCreated, mb2.timeCreated);
		}
	}
}
