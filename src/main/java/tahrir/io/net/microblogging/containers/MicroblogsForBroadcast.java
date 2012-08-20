package tahrir.io.net.microblogging.containers;

import java.util.*;

import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

import com.google.common.collect.Sets;

public class MicroblogsForBroadcast {
	private final PriorityQueue<MicroblogForBroadcast> microblogBroadcastQueue;
	private final Set<Integer> seen;

	public MicroblogsForBroadcast() {
		microblogBroadcastQueue = new PriorityQueue<MicroblogForBroadcast>(11, new MicroblogPriorityComparator());
		seen =  Sets.newLinkedHashSet();
	}

	public MicroblogForBroadcast getMicroblogForBroadcast() {
		return microblogBroadcastQueue.poll();
	}

	public void changeBroadcastPriority(final MicroblogForBroadcast mb, final int priority) {
		microblogBroadcastQueue.remove(mb);
		mb.priority = priority;
		microblogBroadcastQueue.add(mb);
	}

	public boolean isLikelyToContain(final int microblogHash) {
		return seen.contains(microblogHash);
	}

	public boolean contains(final MicroblogForBroadcast mb) {
		return microblogBroadcastQueue.contains(mb);
	}

	public boolean insert(final MicroblogForBroadcast mb) {
		final boolean inserted = false;

		seen.add(mb.hashCode());
		// this check probably isn't necessary but just to be sure...
		if (!microblogBroadcastQueue.contains(mb)) {
			// TODO: it doesn't check the size of the queue, it may get too big
			microblogBroadcastQueue.add(mb);
		}
		return inserted;
	}

	public boolean remove(final MicroblogForBroadcast mb) {
		return microblogBroadcastQueue.remove(mb);
	}

	private class MicroblogPriorityComparator implements Comparator<MicroblogForBroadcast> {
		@Override
		public int compare(final MicroblogForBroadcast mb1, final MicroblogForBroadcast mb2) {
			return Double.compare(mb1.priority, mb2.priority);
		}
	}
}