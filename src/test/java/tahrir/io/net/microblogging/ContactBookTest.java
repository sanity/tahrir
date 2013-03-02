package tahrir.io.net.microblogging;

import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.io.crypto.TrCrypto;

import java.io.File;
import java.security.interfaces.RSAPublicKey;

public class ContactBookTest {
	@Test
	public void persistenceTest() throws Exception {
		File contactsFile = File.createTempFile("tahrir", "contacts_persistence_test");
		ContactBook cb = new ContactBook(contactsFile);

		RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
		String name1 = "name1";
		cb.addContact(name1, user1Key);

		RSAPublicKey user2Key = TrCrypto.createRsaKeyPair().a;
		String name2 = "name2";
		cb.addContact(name2, user2Key);

		// need to set to null so we can test loading from the file
		cb = null;
		cb = new ContactBook(contactsFile);

		Assert.assertTrue(cb.hasContact(user1Key));
		Assert.assertTrue(cb.getAlias(user1Key).equals(name1));
		Assert.assertTrue(cb.hasContact(user2Key));
		Assert.assertTrue(cb.getAlias(user2Key).equals(name2));
	}
}
