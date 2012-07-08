package tahrir.io.net.microblogging;

import java.io.File;
import java.security.interfaces.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.ContactBook.ContactInformation;
import tahrir.tools.*;

public class ContactBookTest {
	@Test
	public void duplicateContactTest() throws Exception {
		final TrNode node = TrUtils.makeTestNode(8001, false, false, false, true, 0, 0);
		final ContactBook contactBook = node.mbManager.contactBook;

		final String name = "name";

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		contactBook.addContact(name, kp1.a);
		contactBook.addContact(name, kp2.a);

		final ContactInformation contact1 = contactBook.contactsContainer.getContact(kp1.a);

		final ContactInformation contact2 = contactBook.contactsContainer.getContact(kp2.a);

		Assert.assertTrue(contact1.getFullNick().equals(name));
		System.out.println(contact2.getFullNick());
	}

	@Test
	public void persistenceTest() throws Exception {
		final TrNode node = TrUtils.makeTestNode(8004, false, false, false, true, 0, 0);
		ContactBook contactBook = node.mbManager.contactBook;

		final String name = "persistence_test_name";

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		contactBook.addContact(name, kp1.a);
		final ContactInformation contact = contactBook.contactsContainer.getContact(kp1.a);

		// remove references to contact book so we can test persistence
		node.mbManager.contactBook = null;
		contactBook = null;

		node.mbManager.contactBook = new ContactBook(node.mbManager, new File(node.rootDirectory, node.config.contacts));
		contactBook = node.mbManager.contactBook;

		final ContactInformation loadedContact = contactBook.contactsContainer.getContact(kp1.a);
		final String loadedNick = loadedContact.getFullNick();

		Assert.assertTrue(contact.getFullNick().equals(loadedNick));
	}
}
