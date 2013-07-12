package tahrir.io.net.microblogging;

import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.containers.MicroblogOutbox;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing;
import tahrir.io.net.microblogging.microblogs.GeneralMicroblogInfo;
import tahrir.io.net.microblogging.microblogs.Microblog;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;
import tahrir.tools.Tuple2;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * Handles things to do with newly incoming microblogs.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class IncomingMicroblogHandler {
	private static final Logger logger = LoggerFactory.getLogger(ParsedMicroblog.class);

	private final MicroblogsForViewing mbsForViewing;
	private final MicroblogOutbox mbsForBroadcast;
    private final IdentityStore identityStore;

	public IncomingMicroblogHandler(final MicroblogsForViewing mbsForViewing,
									final MicroblogOutbox mbsForBroadcast,
									final IdentityStore identityStore) {
		this.mbsForBroadcast = mbsForBroadcast;
		this.mbsForViewing = mbsForViewing;
        this.identityStore = identityStore;
	}

	public void handleInsertion(final Microblog mbForBroadcast) {
		GeneralMicroblogInfo generalMbData = mbForBroadcast.otherData;
		String unparsedMessage = mbForBroadcast.message;
		if (!MicroblogIntegrityChecks.isValidMicroblog(generalMbData, unparsedMessage)) {
			logger.info("A microblog is being ignored because it didn't match required otherData requirements");
			return;
		}
		// try parse the message
		MicroblogParser parser = null;
		try {
			parser = new MicroblogParser(unparsedMessage);
		} catch (final ParsingException e) {
			logger.info("A microblog is being ignored because it failed to be parsed");
			return;
		}
		// the microblog has now passed all the requirements with parsing and otherData constraints
		if (identityStore.hasIdentityInIdStore(generalMbData.getUserIdentity())) {
			// a better priority if they're one of your contacts
			mbForBroadcast.priority -= TrConstants.CONTACT_PRIORITY_INCREASE;
		}
		ParsedMicroblog parsedMb = new ParsedMicroblog(generalMbData, parser.getMentionsFound().keySet(),
				parser.getParsedParts());
        UserIdentity userIdentity=new UserIdentity(generalMbData.getAuthorNick(), generalMbData.getAuthorPubKey());
		addDiscoveredIdentities(new Tuple2<String, UserIdentity>(userIdentity.getNick(), userIdentity));
		mbsForViewing.insert(parsedMb);
		mbsForBroadcast.insert(mbForBroadcast);
	}

	private void addDiscoveredIdentities(Tuple2<String, UserIdentity> fromGeneralData) {
        identityStore.addIdentity(fromGeneralData.b);
	}
}
