package tahrir.io.net.microblogging;

import tahrir.io.net.microblogging.broadcastMessages.GeneralBroadcastMessageInfo;

public class BroadcastMessageIntegrityChecks {
	public static boolean isValidMicroblog(GeneralBroadcastMessageInfo generalBroadcastMessageInfo, String unparsedMessage) {
		// TODO: check the size, author, sig etc.
		return true;
	}
}
