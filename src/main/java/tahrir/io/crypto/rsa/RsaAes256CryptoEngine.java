package tahrir.io.crypto.rsa;

import java.math.BigInteger;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import tahrir.io.Serializer;
import tahrir.io.crypto.*;

/**
 * A simple implementation of the RSA algorithm
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 */
public class RsaAes256CryptoEngine extends CryptoEngine {

	private final int keySize;

	private static SecureRandom sRand = new SecureRandom();

	public RsaAes256CryptoEngine(final Serializer serializer) {
		this(2048, serializer);
	}

	public RsaAes256CryptoEngine(final int keySize, final Serializer serializer) {
		super(serializer);
		this.keySize = keySize;
	}

	// public final BigInteger encrypt(final BigInteger message) {
	// return message.modPow(e, n);
	// }
	//
	// public final BigInteger decrypt(final BigInteger message) {
	// return message.modPow(d, n);
	// }

	@Override
	public AsymmetricKeyPair createKeyPair() {
		final SecureRandom r = new SecureRandom();
		final BigInteger p = new BigInteger(keySize / 2, 100, r);
		final BigInteger q = new BigInteger(keySize / 2, 100, r);
		final BigInteger n = p.multiply(q);
		final BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		BigInteger e = new BigInteger("3");
		while (m.gcd(e).intValue() > 1) {
			e = e.add(new BigInteger("2"));
		}

		final BigInteger d = e.modInverse(m);

		final RsaPublicKey pubkey = new RsaPublicKey(e, n, this);
		final RsaPrivateKey privkey = new RsaPrivateKey(d, n, this);

		return new AsymmetricKeyPair(pubkey, privkey);
	}

	@Override
	public SymmetricKey createKey() {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
			kgen.init(256);
			final SecretKey skey = kgen.generateKey();
			return new AES256SymmetricKey(skey.getEncoded());
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static class AES256SymmetricKey implements SymmetricKey {

		private final SecretKeySpec skey;

		protected AES256SymmetricKey(final BigInteger skey) {
			this(skey.toByteArray());
		}

		protected AES256SymmetricKey(final byte[] key) {
			skey = new SecretKeySpec(key, "AES");
		}

		public byte[] encrypt(final byte[] toEncrypt) {
			try {
				final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
				cipher.init(Cipher.ENCRYPT_MODE, skey, sRand);
				return cipher.doFinal(toEncrypt);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}

		}

		public byte[] decrypt(final byte[] toDecrypt) {
			try {
				final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
				cipher.init(Cipher.DECRYPT_MODE, skey, sRand);
				return cipher.doFinal(toDecrypt);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}

	}
}