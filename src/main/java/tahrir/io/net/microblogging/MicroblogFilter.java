package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPublicKey;
import java.util.LinkedList;

import tahrir.io.net.microblogging.MicrobloggingManger.Microblog;
import tahrir.io.net.microblogging.MicrobloggingManger.MicroblogContainer;

import com.google.common.collect.Lists;

public class MicroblogFilter {
	MicroblogContainer microbloggingContainer;
	ContactBook contactBook;

	public MicroblogFilter(final MicroblogContainer microbloggingContainer, final ContactBook contactBook) {
		this.microbloggingContainer = microbloggingContainer;
		this.contactBook = contactBook;
	}

	public LinkedList<Microblog> getMicroblogsFromContacts() {
		final LinkedList<Microblog> microblogsFromContacts = Lists.newLinkedList();

		for (final Microblog microblog : microbloggingContainer.getMicroblogsForViewing()) {
			// TODO: should verify signature or maybe should do this at a different level
			if (contactBook.contactsContainer.getContact(microblog.publicKey) != null) {
				microblogsFromContacts.add(microblog);
			}
		}
		return microblogsFromContacts;
	}

	public LinkedList<Microblog> getMicroblogsFromUser(final RSAPublicKey userPubKey) {
		final LinkedList<Microblog> microblogsFromUser = Lists.newLinkedList();

		for (final Microblog microblog : microbloggingContainer.getMicroblogsForViewing()) {
			if (microblog.publicKey.equals(userPubKey)) {
				microblogsFromUser.add(microblog);
			}
		}
		return microblogsFromUser;
	}

	public LinkedList<Microblog> getAllMicroblogs() {
		final LinkedList<Microblog> microblogs = Lists.newLinkedList();

		for (final Microblog microblog : microbloggingContainer.getMicroblogsForViewing()) {
			microblogs.add(microblog);
		}
		return microblogs;
	}
}
