package tahrir.io.net.microblogging;

import tahrir.io.net.microblogging.containers.*;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

/**
 * Handles things to do with newly incoming microblogs.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 *
 */
public class IncomingMicroblogHandler {
	MicroblogsForViewing mbsForViewing;
	MicroblogsForBroadcast mbsForBroadcast;

	public IncomingMicroblogHandler(final MicroblogsForViewing mbsForViewing, final MicroblogsForBroadcast mbsForBroadcast) {
		this.mbsForBroadcast = mbsForBroadcast;
		this.mbsForViewing = mbsForViewing;
	}

	public void handleInsertion(final MicroblogForBroadcast mbForBroadcast) {
		microblogsForViewing.add(mb);
	}
}
