package tahrir.io.crypto;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TrSymKeyTest {
	@Test
	public void testEncryptDecrypt() {
		final TrSymKey key = TrCrypto.createAesKey();
		final Random r = new Random();
		final byte[] plainText = new byte[512];
		r.nextBytes(plainText);
		System.out.format("PlainText size: %s bytes%n", plainText.length);
		final byte[] cypherText = key.encrypt(plainText);
		System.out.format("CypherText size: %s bytes%n", cypherText.length);
		final byte[] decryptedCypherText = key.decrypt(cypherText);
		Assert.assertEquals(decryptedCypherText, plainText);
	}
}
