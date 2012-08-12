package tahrir.io.net.microblogging;

import java.util.*;

import tahrir.TrConstants;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

public class MicroblogContainer {
	private final PriorityQueue<Microblog> microblogBroadcastQueue = new PriorityQueue<Microblog>(11, new MicroblogPriorityComparator());
	private final Set<Integer> seen = Sets.newLinkedHashSet();

	private final MicroblogsForViewing microblogsForViewing = new MicroblogsForViewing();

	private final ContactBook contactBook;

	public final EventBus eventBus = new EventBus();

	public MicroblogContainer(final ContactBook contactBook) {
		this.contactBook = contactBook;
	}

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
		if (microblogsForViewing.add(mb)) {
			eventBus.post(mb);
		}
	}

	public Iterator<Microblog> getMicroblogsViewingIter() {
		return microblogsForViewing.iterator();
	}


	private class MicroblogsForViewing implements Iterable<Microblog> {
		private final SortedSet<Microblog> microblogsViewingSet = Collections.synchronizedSortedSet(new TreeSet<Microblog>(new MicroblogTimeComparator()));

		private boolean add(final Microblog mb) {
			// TODO: may want to increase priority if added
			if (microblogsViewingSet.size() < TrConstants.MAX_MICROBLOGS_FOR_VIEWING) {
				microblogsViewingSet.add(mb);
				return true;
			}
			if (shouldAddByReplacement(mb)) {
				microblogsViewingSet.remove(microblogsViewingSet.last());
				microblogsViewingSet.add(mb);
				return true;
			}
			return false;
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

	private class MicroblogPriorityComparator implements Comparator<Microblog> {
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.priority, mb2.priority);
		}
	}

	private class MicroblogTimeComparator implements Comparator<Microblog> {
		// not sure how good this is a way of comparing them
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb1.timeCreated, mb2.timeCreated);
		}
	}
}