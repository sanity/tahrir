package tahrir.io.net.microblogging.containers;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.IdentityStore;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.ui.BroadcastMessageModifiedEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;

/**
 * Stores a collection of microblogs for viewing purposes, normally to display in the GUI.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class BroadcastMessageInbox {
	private static Logger logger = LoggerFactory.getLogger(BroadcastMessageInbox.class);

	private final SortedSet<ParsedMicroblog> parsedMicroblogs;
	private static final ParsedMicroblogTimeComparator comparator =new ParsedMicroblogTimeComparator();

	private final IdentityStore identityStore;
    private final EventBus eventBus;

	public BroadcastMessageInbox(final IdentityStore identityStore) {
		this.identityStore = identityStore;
        eventBus = identityStore.eventBus;
		//comparator = new ParsedMicroblogTimeComparator();
        SortedSet<ParsedMicroblog> tmpSet = Sets.newTreeSet(comparator);
		parsedMicroblogs = Collections.synchronizedSortedSet(tmpSet);
	}

	// synchronised to ensure that size of set is checked properly
	public synchronized boolean insert(final ParsedMicroblog mb) {
		// the microblog might not be added
		boolean inserted = false;

		if (!isFull()) {
			addToParsed(mb);
			inserted = true;
		} else if (shouldAddByReplacement(mb)) {
			// make room
			removeFromParsed(parsedMicroblogs.last());
			addToParsed(mb);
			inserted = true;
			logger.info("Adding a microblog for viewing by replacement");
		}
		return inserted;
	}

	public SortedSet<ParsedMicroblog> getMicroblogSet() {
		return parsedMicroblogs;
	}

	private void removeFromParsed(final ParsedMicroblog mb) {
		parsedMicroblogs.remove(mb);
        logger.debug("Going to post to eventBus for removing a broadcastMessage");
        eventBus.post(new BroadcastMessageModifiedEvent(mb, BroadcastMessageModifiedEvent.ModificationType.REMOVE));
        logger.debug("Event posted.");
	}

	private void addToParsed(final ParsedMicroblog mb) {
		parsedMicroblogs.add(mb);
        logger.debug("Going to  post to eventBus for adding a BroadcastMessage");
        eventBus.post(new BroadcastMessageModifiedEvent(mb, BroadcastMessageModifiedEvent.ModificationType.RECEIVED));
        logger.debug("Event posted (adding)");
	}

	private boolean shouldAddByReplacement(final ParsedMicroblog mb) {
		return identityStore.hasIdentityInIdStore(mb.getMbData().getAuthor()) || isNewerThanLast(mb);
	}

	private boolean isNewerThanLast(final ParsedMicroblog mb) {
		return comparator.compare(mb, parsedMicroblogs.last()) < 0;
	}

	private boolean isFull() {
		return parsedMicroblogs.size() > TrConstants.MAX_MICROBLOGS_FOR_VIEWING;
	}

	public static class ParsedMicroblogTimeComparator implements Comparator<ParsedMicroblog> {
		@Override
		public int compare(final ParsedMicroblog mb1, final ParsedMicroblog mb2) {
			return Double.compare(mb2.getMbData().getTimeCreated(), mb1.getMbData().getTimeCreated());
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