package tahrir.io.net.microblogging;

import java.io.File;
import java.security.interfaces.RSAPublicKey;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.TrNode;
import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.microblogging.ContactBook.ContactInformation;
import tahrir.tools.TrUtils;

public class ContactBookTest {
	@Test
	public void persistenceTest() throws Exception {
		// TODO: slow test because of node creation here?
		final TrNode node = TrUtils.makeTestNode(8004, false, false, false, true, 0, 0);
		ContactBook contactBook = node.mbClasses.contactBook;

		final String nameToAdd = "persistence_test_name";
		final RSAPublicKey keyToAdd = TrCrypto.createRsaKeyPair().a;

		contactBook.addContact(nameToAdd, keyToAdd);
		final ContactInformation contact = contactBook.getContact(keyToAdd);

		// remove references to contact book so we can test persistence
		node.mbClasses.contactBook = null;
		contactBook = null;

		node.mbClasses.contactBook = new ContactBook(new File(node.rootDirectory, node.config.contacts));
		contactBook = node.mbClasses.contactBook;

		final ContactInformation loadedContact = contactBook.getContact(keyToAdd);
		final String loadedNick = loadedContact.getNickName();

		Assert.assertTrue(loadedNick.equals(nameToAdd));
	}
}
