package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

public interface MicroblogFilter {
	public boolean passesFilter(MicroblogForBroadcast mb);
}
