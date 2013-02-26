package tahrir.io.net.microblogging;

import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.containers.MicroblogsForBroadcast;
import tahrir.io.net.microblogging.containers.MicroblogsForViewing;
import tahrir.io.net.microblogging.microblogs.BroadcastMicroblog;
import tahrir.io.net.microblogging.microblogs.GeneralMicroblogInfo;
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
			parser.parseMessage();
		} catch (final ParsingException e) {
			logger.info("A microblog is being ignored because it failed to be parsed");
			return;
		}
		// the microblog has now passed all the requirements with parsing and otherData constraints
		if (contactBook.hasContact(generalMbData.authorPubKey)) {
			// a better priority if they're one of your contacts
			mbForBroadcast.priority -= TrConstants.CONTACT_PRIORITY_INCREASE;
		}
		ParsedMicroblog parsedMb = new ParsedMicroblog(generalMbData, parser.getMentions(), parser.getText());
		addDiscoveredIdentities(parser.getIdentitiesDiscovered(),
				new Tuple2<RSAPublicKey, String>(generalMbData.authorPubKey, generalMbData.authorNick));
		mbsForViewing.insert(parsedMb);
		mbsForBroadcast.insert(mbForBroadcast);
	}

	private void addDiscoveredIdentities(Map<RSAPublicKey, String> fromParsing,
										 Tuple2<RSAPublicKey, String> fromGeneralData) {
		idMap.addNewIdentity(fromGeneralData.a, fromGeneralData.b);
		idMap.addNewIdentities(fromParsing);
	}
}
