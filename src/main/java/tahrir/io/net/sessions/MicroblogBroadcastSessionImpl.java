package tahrir.io.net.sessions;

import java.security.interfaces.RSAPublicKey;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.tools.TrUtils;

/**
 * A class for broadcasting microblogs among nodes.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogBroadcastSessionImpl extends TrSessionImpl implements MicoblogBroadcastSession {
	private static MicroblogQueue microblogQueue;

	static {
		microblogQueue = new MicroblogQueue();

		TrUtils.executor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				microblogQueue.increaseTSU();
			}
		},0, 30, TimeUnit.SECONDS);
	}

	public MicroblogBroadcastSessionImpl(final Integer sessionId, final TrNode node, final TrSessionManager sessionMgr) {
		super(sessionId, node, sessionMgr);
	}

	public void startBroadcast() {
		final Microblog mb = MicroblogBroadcastSessionImpl.microblogQueue.getMicroblogForBroadcast();

		if (mb != null) {
			for (final PhysicalNetworkLocation physicalLocation : node.peerManager.peers.keySet()) {
				final MicoblogBroadcastSession peerSession = remoteSession(MicoblogBroadcastSession.class, connection(physicalLocation));
				peerSession.insertMicroblog(mb);
			}
		}
	}

	public void insertMicroblog(final Microblog mb) {
		MicroblogBroadcastSessionImpl.microblogQueue.insert(mb);
	}

	public static class MicroblogQueue {
		PriorityQueue<Microblog> microBlogs = new PriorityQueue<Microblog>();

		public void insert(final Microblog mb) {
			if (!microBlogs.contains(mb)) {
				microBlogs.add(mb);
			}
		}

		public Microblog getMicroblogForBroadcast() {
			final Microblog mb =  microBlogs.poll();
			if (mb != null) {
				mb.timeSinceUseful++;
				microBlogs.add(mb);
			}
			return mb;
		}

		public void increaseTSU() {
			for (final Microblog mb : microBlogs) {
				// doesn't need to be removed because order remains the same as all are incremented
				mb.timeSinceUseful++;
			}
		}

		public void resetPriority(final Microblog mb) {
			// remove and reinsert with new priority
			microBlogs.remove(mb);
			mb.timeSinceUseful = TrConstants.BROADCAST_INIT_TSU;
			microBlogs.add(mb);
		}
	}

	public static class Microblog implements Comparable<Microblog> {
		public int timeSinceUseful;
		final private String languageCode;
		final private String authorNick;
		final private RSAPublicKey signature;
		final private String message;
		final private long timeCreated;

		public Microblog (final TrNode creatingNode, final String message) {
			timeSinceUseful = TrConstants.BROADCAST_INIT_TSU;
			timeCreated = System.currentTimeMillis();
			this.message = message;
			languageCode = ""; // TODO: get language code from config?
			authorNick = ""; // TODO: get nick from config?
			signature = creatingNode.getRemoteNodeAddress().publicKey;
		}

		@Override
		public int compareTo(final Microblog mb) {
			// a lower TSU should be greater
			return -1 * Integer.compare(timeSinceUseful, mb.timeSinceUseful);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((authorNick == null) ? 0 : authorNick.hashCode());
			result = prime * result + ((languageCode == null) ? 0 : languageCode.hashCode());
			result = prime * result + ((message == null) ? 0 : message.hashCode());
			result = prime * result + ((signature == null) ? 0 : signature.hashCode());
			result = prime * result + (int) (timeCreated ^ (timeCreated >>> 32));
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
			if (authorNick == null) {
				if (other.authorNick != null)
					return false;
			} else if (!authorNick.equals(other.authorNick))
				return false;
			if (languageCode == null) {
				if (other.languageCode != null)
					return false;
			} else if (!languageCode.equals(other.languageCode))
				return false;
			if (message == null) {
				if (other.message != null)
					return false;
			} else if (!message.equals(other.message))
				return false;
			if (signature == null) {
				if (other.signature != null)
					return false;
			} else if (!signature.equals(other.signature))
				return false;
			if (timeCreated != other.timeCreated)
				return false;
			return true;
		}
	}
}
