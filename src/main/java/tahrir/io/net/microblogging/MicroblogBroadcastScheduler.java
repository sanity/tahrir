package tahrir.io.net.microblogging;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrNode;
import tahrir.io.net.PhysicalNetworkLocation;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.tools.TrUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Schedules a single microblog for broadcast to each peer one at a time.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogBroadcastScheduler {
	private static Logger log = LoggerFactory.getLogger(MicroblogBroadcastScheduler.class);

	private final TrNode node;

	private BroadcastMicroblog currentlyBroadcasting;
	private Iterator<PhysicalNetworkLocation> peerIter;

	private boolean disabled = false;

	public MicroblogBroadcastScheduler(final TrNode node) {
		this.node = node;
		scheduleForLater();
	}

	/**
	 * Disable broadcasting, it won't broadcast anymore broadcasts that haven't already been scheduled. Useful to
	 * testing.
	 */
	protected void disable() {
		disabled = true;
	}

	protected void setupForNextMicroblog() {
		final Map<PhysicalNetworkLocation, TrPeerInfo> peerMap = node.peerManager.peers;

		// we don't want to iterate over any new peers added as we could be there forever
		// so we take a snap shot of the set
		final Set<PhysicalNetworkLocation> snapShot = new HashSet<PhysicalNetworkLocation>();

		for (final TrPeerInfo peerInfo : peerMap.values()) {
			if (peerInfo.capabilities.receivesMessageBroadcasts) {
				snapShot.add(peerInfo.remoteNodeAddress.physicalLocation);
			} else {
				if (log.isDebugEnabled()) log.debug("Not broadcasting to seed node");
			}
		}

		// don't want to clog up network if we don't have minimum peers
		if (snapShot.size() >= node.peerManager.config.minPeers) {
			peerIter = snapShot.iterator();
			currentlyBroadcasting = node.mbClasses.mbsForBroadcast.getMicroblogForBroadcast();
			if (log.isDebugEnabled()) log.debug("Broadcasting microblog to a peer");
			startBroadcastToPeer();
		} else {
			if (log.isDebugEnabled()) log.debug("Minimum peers for broadcast was not met. Scheduling a broadcast for later.");
			scheduleForLater();
		}
	}

	protected void startBroadcastToPeer() {
		if (peerIter.hasNext()) {
			final PhysicalNetworkLocation currentPeer = peerIter.next();
			final MicroblogBroadcastSessionImpl localBroadcastSess = node.sessionMgr.getOrCreateLocalSession(MicroblogBroadcastSessionImpl.class);
			localBroadcastSess.startSingleBroadcast(currentlyBroadcasting, currentPeer);
		} else if (!disabled) {
			setupForNextMicroblog();
		} else {
			log.info("Broadcasting disabled");
		}
	}

	private void scheduleForLater() {
		TrUtils.executor.schedule(new RunBroadcast(), 1, TimeUnit.MINUTES);
	}

	private class RunBroadcast implements Runnable {
		public void run() {
			setupForNextMicroblog();
		}
	}
}
