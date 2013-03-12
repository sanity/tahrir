package tahrir.io.crypto;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.tools.ByteArraySegment;

public class TrSymKeyTest {
	@Test
	public void testEncryptDecrypt() {
		final TrSymKey key = TrCrypto.createAesKey();
		final Random r = new Random();
		final byte[] plainText_ = new byte[512];
		r.nextBytes(plainText_);
		System.out.format("PlainText size: %s bytes%n", plainText_.length);
		final ByteArraySegment plainText = new ByteArraySegment(plainText_);
		final ByteArraySegment cypherText = key.encrypt(plainText);
		System.out.format("CypherText size: %s bytes%n", cypherText.length);
		final ByteArraySegment decryptedCypherText = key.decrypt(cypherText);
		Assert.assertEquals(decryptedCypherText, plainText);
	}

	@Test
	public void testSymKeySize(){
		final TrSymKey key = TrCrypto.createAesKey();
		Assert.assertEquals(key.toBytes().length, 16);

	}
}
