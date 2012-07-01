package tahrir.io.net.microblogging;

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
		final TrNode node = TrUtils.makeTestNode(8001, false, false, false, false, 0, 0);

		final String name = "name";

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp2 = TrCrypto.createRsaKeyPair();

		node.contactBook.addContact(name, kp1.a);
		node.contactBook.addContact(name, kp2.a);

		final ContactInformation contact1 = node.contactBook.contactsInformation.getContact(kp1.a);

		final ContactInformation contact2 = node.contactBook.contactsInformation.getContact(kp2.a);

		Assert.assertTrue(contact1.getFullNick().equals(name));
		System.out.println(contact2.getFullNick());
	}

	@Test
	public void persistenceTest() throws Exception {
		final TrNode node = TrUtils.makeTestNode(8004, false, false, false, false, 0, 0);

		final String name = "name";

		final Tuple2<RSAPublicKey, RSAPrivateKey> kp1 = TrCrypto.createRsaKeyPair();

		node.contactBook.addContact(name, kp1.a);
		final ContactInformation contact = node.contactBook.contactsInformation.getContact(kp1.a);

		node.contactBook = null;
		node.contactBook = new ContactBook(node);

		final ContactInformation loadedContact = node.contactBook.contactsInformation.getContact(kp1.a);

		Assert.assertTrue(contact.getFullNick().equals(loadedContact.getFullNick()));
	}
}
