package tahrir.io.net.broadcasts.containers;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
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

	private final SortedSet<BroadcastMessage> broadcastMessages;
	private static final BroadcastMessageTimeComparator comparator =new BroadcastMessageTimeComparator();

	private final IdentityStore identityStore;
    private final EventBus eventBus;

	public BroadcastMessageInbox(final IdentityStore identityStore) {
		this.identityStore = identityStore;
        eventBus = identityStore.eventBus;
		//comparator = new ParsedMicroblogTimeComparator();
        SortedSet<BroadcastMessage> tmpSet = Sets.newTreeSet(comparator);
		broadcastMessages = Collections.synchronizedSortedSet(tmpSet);
	}

	// synchronised to ensure that size of set is checked properly
	public synchronized boolean insert(final BroadcastMessage bm) {
		// the microblog might not be added
		boolean inserted = false;

		if (!isFull()) {
			addToBroadcastMessages(bm);
			inserted = true;
            logger.info("Added broadcast message to inbox.");
		} else if (shouldAddByReplacement(bm)) {
			// make room
			removeFromBroadcastMessages(broadcastMessages.last());
			addToBroadcastMessages(bm);
			inserted = true;
			logger.info("Adding a microblog for viewing by replacement");
		}
		return inserted;
	}

	public SortedSet<BroadcastMessage> getMicroblogSet() {
		return broadcastMessages;
	}

	private void removeFromBroadcastMessages(final BroadcastMessage bm) {
		broadcastMessages.remove(bm);
        eventBus.post(new BroadcastMessageModifiedEvent(bm, BroadcastMessageModifiedEvent.ModificationType.REMOVE));
	}

	private void addToBroadcastMessages(final BroadcastMessage bm) {
		broadcastMessages.add(bm);
        eventBus.post(new BroadcastMessageModifiedEvent(bm, BroadcastMessageModifiedEvent.ModificationType.RECEIVED));
	}

	private boolean shouldAddByReplacement(final BroadcastMessage bm) {
		return identityStore.hasIdentityInIdStore(bm.signedBroadcastMessage.getAuthor()) || isNewerThanLast(bm);
	}

	private boolean isNewerThanLast(final BroadcastMessage bm) {
		return comparator.compare(bm, broadcastMessages.last()) < 0;
	}

	private boolean isFull() {
		return broadcastMessages.size() > TrConstants.MAX_MICROBLOGS_FOR_VIEWING;
	}

	public static class BroadcastMessageTimeComparator implements Comparator<BroadcastMessage> {
		@Override
		public int compare(final BroadcastMessage mb1, final BroadcastMessage mb2) {
			return Double.compare(mb2.signedBroadcastMessage.parsedBroadcastMessage.getTimeCreated(), mb1.signedBroadcastMessage.parsedBroadcastMessage.getTimeCreated());
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