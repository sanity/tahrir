package tahrir.io.net.microblogging.containers;

import java.util.*;

import org.slf4j.*;

import tahrir.TrConstants;
import tahrir.io.net.microblogging.ContactBook;
import tahrir.io.net.microblogging.microblogs.*;
import tahrir.tools.TrUtils;

/**
 * Stores a collection of microblogs for viewing purposes i.e to display in the GUI.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class MicroblogsForViewing {
	private static Logger logger = LoggerFactory.getLogger(MicroblogsForViewing.class);

	private final SortedSet<ParsedMicroblog> parsedMicroblogs = new TreeSet<ParsedMicroblog>(new ParsedMicroblogTimeComparator());
	private final ContactBook contactBook;
	private final UnparsedMicroblogTimeComparator unparsedComparator;
	private final MicroblogsForBroadcast mbsForBroadcast;

	public MicroblogsForViewing(final ContactBook contactBook, final MicroblogsForBroadcast mbsForBroadcast) {
		this.contactBook = contactBook;
		this.mbsForBroadcast = mbsForBroadcast;
		unparsedComparator = new UnparsedMicroblogTimeComparator();
	}

	public synchronized boolean insert(final Microblog mb) {
		boolean added = false;
		try {
			added = tryAdd(mb);
		} catch (final Exception e) {
			logger.info("Error with added microblog");
			// something is wrong with the microblog - it should not be rebroadcast
			mbsForBroadcast.remove(mb);
		}
		return added;
	}

	public SortedSet<ParsedMicroblog> getMicroblogSet() {
		return parsedMicroblogs;
	}

	private boolean tryAdd(final Microblog mb) throws Exception {
		boolean inserted = false;
		// TODO: may want to increase priority if added
		if (!isFull() || shouldAddByReplacement(mb)) {
			final ParsedMicroblog parsedMb = new ParsedMicroblog(mb);
			addToParsed(parsedMb);
			inserted = true;
			if (isFull()) {
				removeFromParsed(parsedMicroblogs.last());
			}
		}

		return inserted;
	}

	private void removeFromParsed(final ParsedMicroblog mb) {
		parsedMicroblogs.remove(mb);
		// also needs to be removed from listeners (filters) to avoid wasting memory
		TrUtils.eventBus.post(new MicroblogRemovalEvent(mb));
	}

	private void addToParsed(final ParsedMicroblog mb) {
		parsedMicroblogs.add(mb);
		// post event to listeners i.e filters
		TrUtils.eventBus.post(new MicroblogAddedEvent(mb));
	}

	private boolean shouldAddByReplacement(final Microblog mb) {
		return contactBook.hasContact(mb.publicKey) || isNewerThanLast(mb);
	}

	private boolean isNewerThanLast(final Microblog mb) {
		return unparsedComparator.compare(mb, parsedMicroblogs.last().sourceMb) < 0;
	}

	private boolean isFull() {
		return parsedMicroblogs.size() > TrConstants.MAX_MICROBLOGS_FOR_VIEWING;
	}

	public static class ParsedMicroblogTimeComparator implements Comparator<ParsedMicroblog> {
		@Override
		public int compare(final ParsedMicroblog mb1, final ParsedMicroblog mb2) {
			return Double.compare(mb2.sourceMb.timeCreated, mb1.sourceMb.timeCreated);
		}
	}

	public static class UnparsedMicroblogTimeComparator implements Comparator<Microblog> {
		@Override
		public int compare(final Microblog mb1, final Microblog mb2) {
			return Double.compare(mb2.timeCreated, mb1.timeCreated);
		}
	}

	public static class MicroblogAddedEvent {
		public ParsedMicroblog mb;

		public MicroblogAddedEvent(final ParsedMicroblog mb) {
			this.mb = mb;
		}
	}

	public static class MicroblogRemovalEvent {
		public ParsedMicroblog mb;

		public MicroblogRemovalEvent(final ParsedMicroblog mb) {
			this.mb = mb;
		}
	}
}