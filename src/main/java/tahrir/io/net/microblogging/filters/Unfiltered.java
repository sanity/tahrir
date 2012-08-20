package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

public class Unfiltered implements MicroblogFilter {
	@Override
	public boolean passesFilter(final MicroblogForBroadcast mb) {
		return true;
	}
}
