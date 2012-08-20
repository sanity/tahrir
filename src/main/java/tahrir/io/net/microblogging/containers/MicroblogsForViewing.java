package tahrir.io.net.microblogging.containers;

import java.util.*;

import tahrir.TrConstants;
import tahrir.io.net.microblogging.ContactBook;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

public class MicroblogsForViewing implements Iterable<MicroblogForBroadcast> {
	private final SortedSet<MicroblogForBroadcast> microblogsViewingSet = Collections.synchronizedSortedSet(new TreeSet<MicroblogForBroadcast>(new MicroblogTimeComparator()));
	private final ContactBook contactBook;
	private final MicroblogTimeComparator timeComparator;

	public MicroblogsForViewing(final ContactBook contactBook) {
		this.contactBook = contactBook;
		timeComparator = new MicroblogTimeComparator();
	}

	public boolean insert(final MicroblogForBroadcast mb) {
		boolean inserted = false;
		// TODO: may want to increase priority if added
		if (!isFull() || shouldAddByReplacement(mb)) {
			microblogsViewingSet.add(mb);
			inserted = true;
			if (isFull()) {
				microblogsViewingSet.remove(microblogsViewingSet.last());
			}
		}
		return inserted;
	}

	@Override
	public Iterator<MicroblogForBroadcast> iterator() {
		return microblogsViewingSet.iterator();
	}

	private boolean shouldAddByReplacement(final MicroblogForBroadcast mb) {
		return contactBook.hasContact(mb.publicKey) || isNewerThanLast(mb);
	}

	private boolean isNewerThanLast(final MicroblogForBroadcast mb) {
		return timeComparator.compare(microblogsViewingSet.last(), mb) > 0;
	}

	private boolean isFull() {
		return microblogsViewingSet.size() > TrConstants.MAX_MICROBLOGS_FOR_VIEWING;
	}

	private class MicroblogTimeComparator implements Comparator<MicroblogForBroadcast> {
		@Override
		public int compare(final MicroblogForBroadcast mb1, final MicroblogForBroadcast mb2) {
			return Double.compare(mb2.timeCreated, mb1.timeCreated);
		}
	}
}