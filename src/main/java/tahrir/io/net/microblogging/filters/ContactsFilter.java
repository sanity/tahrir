package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.IdentityStore;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.util.SortedSet;

public class ContactsFilter extends MicroblogFilter {
	private final IdentityStore identityStore;

	public ContactsFilter(final SortedSet<ParsedMicroblog> initFrom, final IdentityStore identityStore) {
		super();
		this.identityStore = identityStore;
		initMicroblogStorage(initFrom);
	}

	@Override
	public boolean passesFilter(final ParsedMicroblog parsedMb) {
        //compare author nick and their public keys
        return identityStore.hasIdentityInLabel(parsedMb.getMbData().getUserIdentity());
	}
}
