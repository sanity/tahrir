package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.Microblog;

public class Unfiltered implements MicroblogFilter {
	@Override
	public boolean passesFilter(final Microblog mb) {
		return true;
	}
}
