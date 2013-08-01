package tahrir.io.net.broadcasts;

import tahrir.io.net.broadcasts.broadcastMessages.GeneralBroadcastMessageInfo;

public class BroadcastMessageIntegrityChecks {
	public static boolean isValidMicroblog(String unparsedMessage) {
		// TODO: check the size, author, sig etc.
		return true;
	}
}
