package tahrir.io.net.microblogging;

import java.util.*;
import java.util.concurrent.TimeUnit;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.TrPeerManager.TrPeerInfo;
import tahrir.io.net.microblogging.containers.MicroblogsForBroadcast;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;
import tahrir.tools.TrUtils;

/**
 * Schedules a single microblog for broadcast.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogBroadcastScheduler {
	private MicroblogsForBroadcast mbsForBroadcast;
	private TrNode node;

	private MicroblogForBroadcast currentlyBroadcasting;
	private Iterator<PhysicalNetworkLocation> peerIter;

	public MicroblogBroadcastScheduler(final TrNode node) {
		// some nodes i.e seed nodes won't be running microblogging broadcast
		if (node.config.peers.runBroadcast) {
			this.node = node;
			scheduleForLater();

			mbsForBroadcast = new MicroblogsForBroadcast();
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

			currentlyBroadcasting = mbsForBroadcast.getMicroblogForBroadcast();

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

	public MicroblogsForBroadcast getMicroblogContainer() {
		return mbsForBroadcast;
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
