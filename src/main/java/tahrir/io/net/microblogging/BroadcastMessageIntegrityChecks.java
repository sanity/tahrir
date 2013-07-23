package tahrir.io.net.microblogging;

import tahrir.io.net.microblogging.microblogs.GeneralMicroblogInfo;

public class BroadcastMessageIntegrityChecks {
	public static boolean isValidMicroblog(GeneralMicroblogInfo generalMicroblogInfo, String unparsedMessage) {
		// TODO: check the size, author, sig etc.
		return true;
	}
}
