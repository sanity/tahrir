package tahrir.io.net.microblogging.containers;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.IdentityStore;
import tahrir.io.net.microblogging.broadcastMessages.ParsedBroadcastMessage;
import tahrir.ui.BroadcastMessageModifiedEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;

/**
 * Stores a collection of broadcastMessages for viewing purposes, normally to display in the GUI.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class BroadcastMessageInbox {
	private static Logger logger = LoggerFactory.getLogger(BroadcastMessageInbox.class);

	private final SortedSet<ParsedBroadcastMessage> parsedBroadcastMessages;
	private static final ParsedMicroblogTimeComparator comparator =new ParsedMicroblogTimeComparator();

	private final IdentityStore identityStore;
    private final EventBus eventBus;

	public BroadcastMessageInbox(final IdentityStore identityStore) {
		this.identityStore = identityStore;
        eventBus = identityStore.eventBus;
		//comparator = new ParsedMicroblogTimeComparator();
        SortedSet<ParsedBroadcastMessage> tmpSet = Sets.newTreeSet(comparator);
		parsedBroadcastMessages = Collections.synchronizedSortedSet(tmpSet);
	}

	// synchronised to ensure that size of set is checked properly
	public synchronized boolean insert(final ParsedBroadcastMessage mb) {
		// the microblog might not be added
		boolean inserted = false;

		if (!isFull()) {
			addToParsed(mb);
			inserted = true;
		} else if (shouldAddByReplacement(mb)) {
			// make room
			removeFromParsed(parsedBroadcastMessages.last());
			addToParsed(mb);
			inserted = true;
			logger.info("Adding a microblog for viewing by replacement");
		}
		return inserted;
	}

	public SortedSet<ParsedBroadcastMessage> getMicroblogSet() {
		return parsedBroadcastMessages;
	}

	private void removeFromParsed(final ParsedBroadcastMessage mb) {
		parsedBroadcastMessages.remove(mb);
        eventBus.post(new BroadcastMessageModifiedEvent(mb, BroadcastMessageModifiedEvent.ModificationType.REMOVE));
	}

	private void addToParsed(final ParsedBroadcastMessage mb) {
		parsedBroadcastMessages.add(mb);
        eventBus.post(new BroadcastMessageModifiedEvent(mb, BroadcastMessageModifiedEvent.ModificationType.RECEIVED));
	}

	private boolean shouldAddByReplacement(final ParsedBroadcastMessage mb) {
		return identityStore.hasIdentityInIdStore(mb.getMbData().getAuthor()) || isNewerThanLast(mb);
	}

	private boolean isNewerThanLast(final ParsedBroadcastMessage mb) {
		return comparator.compare(mb, parsedBroadcastMessages.last()) < 0;
	}

	private boolean isFull() {
		return parsedBroadcastMessages.size() > TrConstants.MAX_MICROBLOGS_FOR_VIEWING;
	}

	public static class ParsedMicroblogTimeComparator implements Comparator<ParsedBroadcastMessage> {
		@Override
		public int compare(final ParsedBroadcastMessage mb1, final ParsedBroadcastMessage mb2) {
			return Double.compare(mb2.getMbData().getTimeCreated(), mb1.getMbData().getTimeCreated());
		}
	}

	public static class MicroblogAddedEvent {
		public ParsedBroadcastMessage mb;
		public MicroblogAddedEvent(final ParsedBroadcastMessage mb) {
			this.mb = mb;
		}
	}

	public static class MicroblogRemovalEvent {
		public ParsedBroadcastMessage mb;
		public MicroblogRemovalEvent(final ParsedBroadcastMessage mb) {
			this.mb = mb;
		}
	}
}