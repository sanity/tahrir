package tahrir.io.net.microblogging.containers;

import java.util.*;

import tahrir.io.net.microblogging.microblogs.Microblog;

import com.google.common.collect.Sets;

public class MicroblogsForBroadcast {
	private final PriorityQueue<Microblog> microblogBroadcastQueue;
	private final Set<Integer> seen;

	public MicroblogsForBroadcast() {
		microblogBroadcastQueue = new PriorityQueue<Microblog>(100, new MicroblogPriorityComparator());
		seen =  Sets.newLinkedHashSet();
	}

	public synchronized Microblog getMicroblogForBroadcast() {
		return microblogBroadcastQueue.poll();
	}

	public synchronized void changeBroadcastPriority(final Microblog mb, final int priority) {
		microblogBroadcastQueue.remove(mb);
		mb.priority = priority;
		microblogBroadcastQueue.add(mb);
	}

	public synchronized boolean isLikelyToContain(final int microblogHash) {
		return seen.contains(microblogHash);
	}

	public synchronized boolean contains(final Microblog mb) {
		return microblogBroadcastQueue.contains(mb);
	}

	public synchronized boolean insert(final Microblog mb) {
		final boolean inserted = false;

		seen.add(mb.hashCode());
		// this check probably isn't necessary but just to be sure...
		if (!microblogBroadcastQueue.contains(mb)) {
			// TODO: it doesn't check the size of the queue, it may get too big
			microblogBroadcastQueue.add(mb);
		}
		return inserted;
	}

	public synchronized boolean remove(final Microblog mb) {
		return microblogBroadcastQueue.remove(mb);
	}

	private class MicroblogPriorityComparator implements Comparator<Microblog> {
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.priority, mb2.priority);
		}
	}
}