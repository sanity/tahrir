package tahrir.io.net.microblogging;

import tahrir.TrConstants;
import tahrir.io.net.microblogging.containers.*;
import tahrir.io.net.microblogging.microblogs.Microblog;

/**
 * Handles things to do with newly incoming microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class IncomingMicroblogHandler {
	private final MicroblogsForViewing mbsForViewing;
	private final MicroblogsForBroadcast mbsForBroadcast;

	private final ContactBook contactBook;

	public IncomingMicroblogHandler(final MicroblogsForViewing mbsForViewing, final MicroblogsForBroadcast mbsForBroadcast, final ContactBook contactBook) {
		this.mbsForBroadcast = mbsForBroadcast;
		this.mbsForViewing = mbsForViewing;
		this.contactBook = contactBook;
	}

	public void handleInsertion(final Microblog mbForBroadcast) {
		// "increase" priority if they're one of your contacts
		if (contactBook.hasContact(mbForBroadcast.publicKey)) {
			mbForBroadcast.priority -= TrConstants.CONTACT_PRIORITY_INCREASE;
		}

		mbsForBroadcast.insert(mbForBroadcast);
		mbsForViewing.insert(mbForBroadcast);

		// TODO: add to address map
	}
}
