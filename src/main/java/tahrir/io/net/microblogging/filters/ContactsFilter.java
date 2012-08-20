package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.*;
import tahrir.io.net.microblogging.microblogs.MicroblogForBroadcast;

public class ContactsFilter implements MicroblogFilter {
	private final ContactBook contactBook;

	public ContactsFilter(final ContactBook contactBook) {
		this.contactBook = contactBook;
	}

	@Override
	public boolean passesFilter(final MicroblogForBroadcast mb) {
		if (contactBook.contactsContainer.hasContact(mb.publicKey))
			return true;
		else
			return false;
	}
}
