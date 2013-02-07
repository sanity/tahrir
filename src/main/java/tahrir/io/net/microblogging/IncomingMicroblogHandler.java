package tahrir.io.net.microblogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tahrir.TrConstants;
import tahrir.io.net.microblogging.containers.MicroblogsForBroadcast;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

/**
 * Handles things to do with newly incoming microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class IncomingMicroblogHandler {
	private static final Logger logger = LoggerFactory.getLogger(ParsedMicroblog.class);

	private final MicroblogsForViewing mbsForViewing;
	private final MicroblogsForBroadcast mbsForBroadcast;

	private final ContactBook contactBook;
	private final IdentityMap idMap;

	public IncomingMicroblogHandler(final MicroblogsForViewing mbsForViewing,
			final MicroblogsForBroadcast mbsForBroadcast,
			final ContactBook contactBook, final IdentityMap identityMap) {
		this.mbsForBroadcast = mbsForBroadcast;
		this.mbsForViewing = mbsForViewing;
		this.contactBook = contactBook;
		idMap = identityMap;
	}

	public void handleInsertion(final BroadcastMicroblog mbForBroadcast) {
		if (!MicroblogIntegrityChecks.isValidMicroblog(mbForBroadcast)) {
			logger.info("A microblog is being ignored because it didn't match required data requirements");
			return;
		}

		// a better priority if they're one of your contacts
		if (contactBook.hasContact(mbForBroadcast.data.authorPubKey)) {
			mbForBroadcast.priority -= TrConstants.CONTACT_PRIORITY_INCREASE;
		}

		try {
			final ParsedMicroblog parsedMb = new ParsedMicroblog(mbForBroadcast.data, mbForBroadcast.message);
			// it's passed the requirements, time to add it to the queues
			mbsForBroadcast.insert(mbForBroadcast);
			mbsForViewing.insert(parsedMb);
			addDataToAddressMap(parsedMb);
		} catch (final Exception e) {
			logger.info("A microblog is being ignored because it failed to be parsed");
			return;
		}
	}

	private void addDataToAddressMap(final ParsedMicroblog parsedMicroblog) {
		// Some data from the microblog mentions have already been added to the
		// address map via. an event in ParsedMicroblog.
		idMap.addNewIdentity(parsedMicroblog.mbData.authorPubKey, parsedMicroblog.mbData.authorNick);
	}
}
