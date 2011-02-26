package tahrir.io.crypto;

import java.math.BigInteger;

public class RsaPublicKey {

	public final BigInteger e;
	public final BigInteger n;

	public RsaPublicKey(final BigInteger e, final BigInteger n) {
		this.e = e;
		this.n = n;
	}

	protected BigInteger verify(final BigInteger signature) {
		return signature.modPow(e, n);
	}

	public boolean verify(final SHA256Hash hash, final RsaSignature signature) {
		final BigInteger v = verify(signature.signature);
		return v.equals(hash.toBigInteger());
	}

	protected BigInteger encrypt(final BigInteger signature) {
		return signature.modPow(e, n);
	}

	public RsaEncrypted encrypt(final AES256SymmetricKey aesKey) {
		return new RsaEncrypted(new BigInteger(aesKey.toBytes()));
	}
}
