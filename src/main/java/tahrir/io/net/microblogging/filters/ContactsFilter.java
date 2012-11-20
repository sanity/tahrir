package tahrir.io.net.microblogging.filters;

import java.util.SortedSet;

import tahrir.io.net.microblogging.ContactBook;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

public class ContactsFilter extends MicroblogFilter {
	private final ContactBook contactBook;

	public ContactsFilter(final SortedSet<ParsedMicroblog> initFrom, final ContactBook contactBook) {
		super();
		this.contactBook = contactBook;
		initMicroblogStorage(initFrom);
	}

	@Override
	public boolean passesFilter(final ParsedMicroblog parsedMb) {
		return contactBook.hasContact(parsedMb.sourceMb.publicKey);
	}
}
