package tahrir.io.crypto;

import java.nio.ByteBuffer;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TrSymKey {

	private static IvParameterSpec ivSpec;

	static {
		Security.addProvider(new BouncyCastleProvider());
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			final byte[] iv = new byte[cipher.getBlockSize()];
			for (int i = 0; i < iv.length; i++) {
				iv[i] = 0;
			}
			ivSpec = new IvParameterSpec(iv);
		} catch (final NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private final SecretKeySpec skey;

	public TrSymKey(final byte[] bytes) {
		skey = new SecretKeySpec(bytes, "AES");
	}

	public byte[] toBytes() {
		return skey.getEncoded();
	}

	public byte[] encrypt(final byte[] toEncrypt) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");

			cipher.init(Cipher.ENCRYPT_MODE, skey, ivSpec);
			return cipher.doFinal(toEncrypt);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] decrypt(final byte[] toDecrypt) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);
			return cipher.doFinal(toDecrypt);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void decrypt(final ByteBuffer cipherText, final ByteBuffer plainText) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);
			cipher.doFinal(cipherText, plainText);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}