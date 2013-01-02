package tahrir.io.crypto;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import tahrir.tools.ByteArraySegment;
import tahrir.tools.ByteArraySegment.ByteArraySegmentBuilder;

public class TrSymKey {

	private static final String CIPHER_NAME = "AES/CBC/PKCS7Padding";

	private static final int BLOCK_SIZE;

	static {
		Security.addProvider(new BouncyCastleProvider());

		try {
			BLOCK_SIZE = Cipher.getInstance(CIPHER_NAME).getBlockSize();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final SecureRandom rng = new SecureRandom();

	private final SecretKeySpec skey;

	public TrSymKey(final ByteArraySegment bas) {
		skey = new SecretKeySpec(bas.array, bas.offset, bas.length, "AES");
	}

	public byte[] toBytes() {
		return skey.getEncoded();
	}

	public ByteArraySegment decrypt(final ByteArraySegment toDecrypt) {
		try {
			final Cipher cipher = Cipher.getInstance(CIPHER_NAME);
			final IvParameterSpec ivSpec = new IvParameterSpec(toDecrypt.array, toDecrypt.offset, BLOCK_SIZE);
			cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);
			return new ByteArraySegment(cipher.doFinal(toDecrypt.array, toDecrypt.offset+BLOCK_SIZE, toDecrypt.length-BLOCK_SIZE));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ByteArraySegment encrypt(final ByteArraySegment toEncrypt) {
		try {
			final byte[] iv = new byte[BLOCK_SIZE];
			synchronized (rng) {
				rng.nextBytes(iv);
			}
			final Cipher cipher = Cipher.getInstance(CIPHER_NAME);
			cipher.init(Cipher.ENCRYPT_MODE, skey, new IvParameterSpec(iv));
			final byte[] ciphertext = cipher.doFinal(toEncrypt.array, toEncrypt.offset, toEncrypt.length);
			final ByteArraySegmentBuilder basb = new ByteArraySegmentBuilder();
			basb.write(iv);
			basb.write(ciphertext);
			return basb.build();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ByteArraySegment toByteArraySegment() {
		return new ByteArraySegment(toBytes());
	}
}