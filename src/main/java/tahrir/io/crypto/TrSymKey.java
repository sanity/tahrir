package tahrir.io.crypto;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import tahrir.tools.ByteArraySegment;

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

	public TrSymKey(final ByteArraySegment bas) {
		skey = new SecretKeySpec(bas.array, bas.offset, bas.length, "AES");
	}

	public byte[] toBytes() {
		return skey.getEncoded();
	}

	public ByteArraySegment decrypt(final ByteArraySegment toDecrypt) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);
			return new ByteArraySegment(cipher.doFinal(toDecrypt.array, toDecrypt.offset, toDecrypt.length));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ByteArraySegment encrypt(final ByteArraySegment toEncrypt) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, skey, ivSpec);
			return new ByteArraySegment(cipher.doFinal(toEncrypt.array, toEncrypt.offset, toEncrypt.length));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ByteArraySegment toByteArraySegment() {
		return new ByteArraySegment(toBytes());
	}
}