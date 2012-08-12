package tahrir.io.net.microblogging.filters;

import tahrir.io.net.microblogging.*;

public class ContactsFilter implements MicroblogFilter {
	private final ContactBook contactBook;

	public ContactsFilter(final ContactBook contactBook) {
		this.contactBook = contactBook;
	}

	@Override
	public boolean passesFilter(final Microblog mb) {
		if (contactBook.contactsContainer.hasContact(mb.publicKey))
			return true;
		else
			return false;
	}
}
