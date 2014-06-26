package tahrir.network.broadcasts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.network.broadcasts.containers.BroadcastMessageInbox;
import tahrir.network.broadcasts.containers.BroadcastMessageOutbox;
import tahrir.network.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.network.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.util.tools.Tuple2;

/**
 * Handles things to do with newly incoming broadcastMessages.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class IncomingBroadcastMessageHandler {
	private static final Logger logger = LoggerFactory.getLogger(ParsedBroadcastMessage.class);

	private final BroadcastMessageInbox mbsForViewing;
	private final BroadcastMessageOutbox mbsForBroadcast;
    private final IdentityStore identityStore;

	public IncomingBroadcastMessageHandler(final BroadcastMessageInbox mbsForViewing,
                                           final BroadcastMessageOutbox mbsForBroadcast,
                                           final IdentityStore identityStore) {
		this.mbsForBroadcast = mbsForBroadcast;
		this.mbsForViewing = mbsForViewing;
        this.identityStore = identityStore;
	}

	public void handleInsertion(final BroadcastMessage mbForBroadcast) {
		if (!BroadcastMessageIntegrityChecks.isValidMicroblog(mbForBroadcast.signedBroadcastMessage.parsedBroadcastMessage.getPlainTextBroadcastMessage())) {
			logger.info("A microblog is being ignored because it didn't match required otherData requirements");
			return;
		}

		// the microblog has now passed all the requirements with parsing and otherData constraints
		if (identityStore.hasIdentityInIdStore(mbForBroadcast.signedBroadcastMessage.getAuthor())) {
			// a better priority if they're one of your contacts
			mbForBroadcast.priority -= TrConstants.CONTACT_PRIORITY_INCREASE;
		}
        UserIdentity userIdentity = mbForBroadcast.signedBroadcastMessage.getAuthor();
		addDiscoveredIdentities(new Tuple2<String, UserIdentity>(userIdentity.getNick(), userIdentity));
		mbsForViewing.insert(mbForBroadcast);
		mbsForBroadcast.insert(mbForBroadcast);
	}

	private void addDiscoveredIdentities(Tuple2<String, UserIdentity> fromGeneralData) {
        identityStore.addIdentity(fromGeneralData.b);
	}
}
