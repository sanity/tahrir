package tahrir.io.crypto;

import java.io.*;
import java.security.*;
import java.security.interfaces.*;

import javax.crypto.*;

import tahrir.TrConstants;
import tahrir.io.serialization.*;
import tahrir.tools.Tuple2;

/**
 * A simple implementation of the RSA algorithm
 * 
 * @author Ian Clarke <ian.clarke@gmail.com>
 */
public class TrCrypto {
	static SecureRandom sRand = new SecureRandom();

	public static Tuple2<RSAPublicKey, RSAPrivateKey> createRsaKeyPair() {
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			final KeyPair key = keyGen.generateKeyPair();
			return Tuple2.of((RSAPublicKey) key.getPublic(), (RSAPrivateKey) key.getPrivate());
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static TrSymKey createAesKey() {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
			kgen.init(256);
			final SecretKey skey = kgen.generateKey();
			return new TrSymKey(skey.getEncoded());
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static Cipher getRSACipher() {
		try {
			return Cipher.getInstance("RSA/None/NoPadding", "BC");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (final NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (final NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public static TrSignature sign(final Object toSign, final RSAPrivateKey privKey) throws TrSerializableException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(TrConstants.DEFAULT_BAOS_SIZE);
		final DataOutputStream dos = new DataOutputStream(baos);
		try {
			TrSerializer.serializeTo(toSign, dos);
			dos.flush();
			final Signature signature = Signature.getInstance("SHA256withRSA", "BC");
			signature.initSign(privKey);
			signature.update(baos.toByteArray());
			return new TrSignature(signature.sign());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean verify(final TrSignature signature, final Object toVerify, final RSAPublicKey pubKey)
	throws TrSerializableException {
		// TODO: We serialize this object and then throw the result away, which
		// is probably wasteful as frequently the object will be serialized
		// elsewhere
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(TrConstants.DEFAULT_BAOS_SIZE);
		final DataOutputStream dos = new DataOutputStream(baos);
		try {
			TrSerializer.serializeTo(toVerify, dos);
			final Signature sig = Signature.getInstance("SHA256withRSA", "BC");
			sig.initVerify(pubKey);
			sig.update(baos.toByteArray());
			return sig.verify(signature.signature);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static <T> TrPPKEncrypted<T> encrypt(final T plainText, final RSAPublicKey pubKey)
	throws TrSerializableException {
		// TODO: Lots of reading from and writing to byte arrays, inefficient
		final ByteArrayOutputStream serializedPlaintext = new ByteArrayOutputStream(TrConstants.DEFAULT_BAOS_SIZE);
		final DataOutputStream dos = new DataOutputStream(serializedPlaintext);
		try {
			TrSerializer.serializeTo(plainText, dos);
			dos.flush();
			final TrSymKey aesKey = createAesKey();
			final byte[] aesEncrypted = aesKey.encrypt(serializedPlaintext.toByteArray());
			final Cipher cipher = getRSACipher();

			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			final byte[] rsaEncryptedAesKey = cipher.doFinal(aesKey.toBytes());
			return new TrPPKEncrypted<T>(rsaEncryptedAesKey, aesEncrypted);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static <T> T decrypt(final Class<T> c, final TrPPKEncrypted<T> cipherText, final RSAPrivateKey privKey) {
		final Cipher cipher = getRSACipher();
		try {
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			final TrSymKey aesKey = new TrSymKey(cipher.doFinal(cipherText.rsaEncryptedAesKey));
			final byte[] serializedPlainTextByteArray = aesKey.decrypt(cipherText.aesCypherText);
			final ByteArrayInputStream bais = new ByteArrayInputStream(serializedPlainTextByteArray);
			return TrSerializer.deserializeFrom(c, new DataInputStream(bais));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}