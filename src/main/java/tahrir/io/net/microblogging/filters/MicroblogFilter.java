package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.Microblog;

public interface MicroblogFilter {
	public boolean passesFilter(Microblog mb);
}
