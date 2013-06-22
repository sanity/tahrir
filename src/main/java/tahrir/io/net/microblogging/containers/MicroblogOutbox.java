package tahrir.io.net.microblogging.containers;

import com.google.common.collect.Sets;
import tahrir.io.net.microblogging.microblogs.Microblog;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

public class MicroblogOutbox {
	private final PriorityQueue<Microblog> outbox;
	private final Set<Integer> seen;

	public MicroblogOutbox() {
		outbox = new PriorityQueue<Microblog>(100, new MicroblogPriorityComparator());
		seen = Sets.newLinkedHashSet();
	}

	public synchronized Microblog getMicroblogForBroadcast() {
		return outbox.poll();
	}

	public synchronized void changeBroadcastPriority(final Microblog mb, final int priority) {
		outbox.remove(mb);
		mb.priority = priority;
		outbox.add(mb);
	}

	public synchronized boolean isLikelyToContain(final int microblogHash) {
		return seen.contains(microblogHash);
	}

	public synchronized boolean contains(final Microblog mb) {
		return outbox.contains(mb);
	}

	public synchronized boolean insert(final Microblog mb) {
		final boolean inserted = false;

		seen.add(mb.hashCode());
		// this check probably isn't necessary but just to be sure...
		if (!outbox.contains(mb)) {
			// TODO: it doesn't check the size of the queue, it may get too big
			outbox.add(mb);
		}
		return inserted;
	}

	public synchronized boolean remove(final Microblog mb) {
		return outbox.remove(mb);
	}

	private class MicroblogPriorityComparator implements Comparator<Microblog> {
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.priority, mb2.priority);
		}
	}
}