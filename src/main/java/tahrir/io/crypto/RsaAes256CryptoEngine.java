package tahrir.io.crypto;

import java.math.BigInteger;
import java.security.*;

import javax.crypto.*;

import tahrir.tools.Tuple2;

/**
 * A simple implementation of the RSA algorithm
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 */
public class RsaAes256CryptoEngine {
	static SecureRandom sRand = new SecureRandom();

	public static Tuple2<RsaPrivateKey, RsaPublicKey> createRsaKeyPair(final int keySize) {
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

		final RsaPublicKey pubkey = new RsaPublicKey(e, n);
		final RsaPrivateKey privkey = new RsaPrivateKey(d, n);

		return Tuple2.of(privkey, pubkey);
	}

	public static AES256SymmetricKey createAesKey() {
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
}