package tahrir.io.net.microblogging;

import com.google.common.base.Optional;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.containers.BroadcastMessageInbox;
import tahrir.io.net.microblogging.containers.BroadcastMessageOutbox;
import tahrir.io.net.microblogging.broadcastMessages.GeneralBroadcastMessageInfo;
import tahrir.io.net.microblogging.broadcastMessages.BroadcastMessage;
import tahrir.io.net.microblogging.broadcastMessages.ParsedBroadcastMessage;
import tahrir.tools.Tuple2;

import java.security.interfaces.RSAPrivateKey;

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
		GeneralBroadcastMessageInfo generalMbData = mbForBroadcast.otherData;
		String unparsedMessage = mbForBroadcast.message;
		if (!BroadcastMessageIntegrityChecks.isValidMicroblog(generalMbData, unparsedMessage)) {
			logger.info("A microblog is being ignored because it didn't match required otherData requirements");
			return;
		}
		// try parse the message
		BroadcastMessageParser parser = null;
		try {
			parser = new BroadcastMessageParser(unparsedMessage);
		} catch (final ParsingException e) {
			logger.info("A microblog is being ignored because it failed to be parsed");
			return;
		}
		// the microblog has now passed all the requirements with parsing and otherData constraints
		if (identityStore.hasIdentityInIdStore(generalMbData.getAuthor())) {
			// a better priority if they're one of your contacts
			mbForBroadcast.priority -= TrConstants.CONTACT_PRIORITY_INCREASE;
		}
		ParsedBroadcastMessage parsedMb = new ParsedBroadcastMessage(generalMbData, parser.getMentionsFound().keySet(),
				parser.getParsedParts());
        UserIdentity userIdentity=new UserIdentity(generalMbData.getAuthorNick(), generalMbData.getAuthorPubKey(), Optional.<RSAPrivateKey>absent());
		addDiscoveredIdentities(new Tuple2<String, UserIdentity>(userIdentity.getNick(), userIdentity));
		mbsForViewing.insert(parsedMb);
		mbsForBroadcast.insert(mbForBroadcast);
	}

	private void addDiscoveredIdentities(Tuple2<String, UserIdentity> fromGeneralData) {
        identityStore.addIdentity(fromGeneralData.b);
	}
}
