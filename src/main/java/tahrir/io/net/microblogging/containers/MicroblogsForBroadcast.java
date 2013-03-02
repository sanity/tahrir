package tahrir.io.net.microblogging.containers;

import com.google.common.collect.Sets;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

public class MicroblogsForBroadcast {
	private final PriorityQueue<BroadcastMicroblog> microblogBroadcastQueue;
	private final Set<Integer> seen;

	public MicroblogsForBroadcast() {
		microblogBroadcastQueue = new PriorityQueue<BroadcastMicroblog>(100, new MicroblogPriorityComparator());
		seen = Sets.newLinkedHashSet();
	}

	public synchronized BroadcastMicroblog getMicroblogForBroadcast() {
		return microblogBroadcastQueue.poll();
	}

	public synchronized void changeBroadcastPriority(final BroadcastMicroblog mb, final int priority) {
		microblogBroadcastQueue.remove(mb);
		mb.priority = priority;
		microblogBroadcastQueue.add(mb);
	}

	public synchronized boolean isLikelyToContain(final int microblogHash) {
		return seen.contains(microblogHash);
	}

	public synchronized boolean contains(final BroadcastMicroblog mb) {
		return microblogBroadcastQueue.contains(mb);
	}

	public synchronized boolean insert(final BroadcastMicroblog mb) {
		final boolean inserted = false;

		seen.add(mb.hashCode());
		// this check probably isn't necessary but just to be sure...
		if (!microblogBroadcastQueue.contains(mb)) {
			// TODO: it doesn't check the size of the queue, it may get too big
			microblogBroadcastQueue.add(mb);
		}
		return inserted;
	}

	public synchronized boolean remove(final BroadcastMicroblog mb) {
		return microblogBroadcastQueue.remove(mb);
	}

	private class MicroblogPriorityComparator implements Comparator<BroadcastMicroblog> {
		@Override
		public int compare(final BroadcastMicroblog mb1, final BroadcastMicroblog mb2) {
			return Double.compare(mb1.priority, mb2.priority);
		}
	}
}