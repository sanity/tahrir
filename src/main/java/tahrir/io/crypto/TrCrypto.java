package tahrir.io.crypto;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.*;

import javax.crypto.*;

import tahrir.io.serialization.*;
import tahrir.tools.Tuple2;
import static tahrir.TrConstants.MAX_BYTEBUFFER_SIZE_BYTES;

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
		final ByteBuffer toSignSerialized = ByteBuffer.allocate(MAX_BYTEBUFFER_SIZE_BYTES);
		TrSerializer.serializeTo(toSign, toSignSerialized);
		toSignSerialized.flip();
		try {
			final Signature signature = Signature.getInstance("SHA256withRSA", "BC");
			signature.initSign(privKey);
			signature.update(toSignSerialized);
			return new TrSignature(signature.sign());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean verify(final TrSignature signature, final Object toVerify, final RSAPublicKey pubKey)
			throws TrSerializableException {
		final ByteBuffer toVerifySerialized = ByteBuffer.allocate(MAX_BYTEBUFFER_SIZE_BYTES);
		TrSerializer.serializeTo(toVerify, toVerifySerialized);
		toVerifySerialized.flip();
		try {
			final Signature sig = Signature.getInstance("SHA256withRSA", "BC");
			sig.initVerify(pubKey);
			sig.update(toVerifySerialized);
			return sig.verify(signature.signature);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static <T> TrPPKEncrypted<T> encrypt(final T plainText, final RSAPublicKey pubKey)
	throws TrSerializableException {
		final ByteBuffer serializedPlaintext = ByteBuffer.allocate(MAX_BYTEBUFFER_SIZE_BYTES);
		TrSerializer.serializeTo(plainText, serializedPlaintext);
		serializedPlaintext.flip();
		final TrSymKey aesKey = createAesKey();
		final ByteBuffer aesEncrypted = ByteBuffer.allocate(MAX_BYTEBUFFER_SIZE_BYTES);
		aesKey.encrypt(serializedPlaintext, aesEncrypted);
		aesEncrypted.flip();
		final Cipher cipher = getRSACipher();
		try {
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			final byte[] rsaEncryptedAesKey = cipher.doFinal(aesKey.toBytes());
			final byte[] aesEncryptedByteArray = new byte[aesEncrypted.remaining()];
			aesEncrypted.get(aesEncryptedByteArray);
			return new TrPPKEncrypted<T>(rsaEncryptedAesKey, aesEncryptedByteArray);
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
			final ByteBuffer bb = ByteBuffer.allocate(serializedPlainTextByteArray.length);
			bb.put(serializedPlainTextByteArray);
			bb.flip();
			return TrSerializer.deserializeFrom(c, bb);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}