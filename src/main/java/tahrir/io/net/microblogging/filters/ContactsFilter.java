package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.ContactBook;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import java.util.SortedSet;

public class ContactsFilter extends MicroblogFilter {
	private final ContactBook contactBook;

	public ContactsFilter(final SortedSet<ParsedMicroblog> initFrom, final ContactBook contactBook) {
		super();
		this.contactBook = contactBook;
		initMicroblogStorage(initFrom);
	}

	@Override
	public boolean passesFilter(final ParsedMicroblog parsedMb) {
		return contactBook.hasContact(parsedMb.getMbData().getAuthorPubKey());
	}
}
