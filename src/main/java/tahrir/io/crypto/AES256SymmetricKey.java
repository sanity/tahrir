package tahrir.io.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES256SymmetricKey {

	private final SecretKeySpec skey;

	public AES256SymmetricKey(final byte[] bytes) {
		skey = new SecretKeySpec(bytes, "AES");
	}

	public byte[] toBytes() {
		return skey.getEncoded();
	}

	public byte[] encrypt(final byte[] toEncrypt) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey, RsaAes256CryptoEngine.sRand);
			return cipher.doFinal(toEncrypt);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	public byte[] decrypt(final byte[] toDecrypt) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey, RsaAes256CryptoEngine.sRand);
			return cipher.doFinal(toDecrypt);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}