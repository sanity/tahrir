package tahrir.io.crypto;

import java.math.BigInteger;

public class RsaPublicKey {

	public final BigInteger e;
	public final BigInteger n;

	public RsaPublicKey(final BigInteger e, final BigInteger n) {
		this.e = e;
		this.n = n;
	}

	public BigInteger verify(final BigInteger signature) {
		return signature.modPow(e, n);
	}

	public BigInteger encrypt(final BigInteger signature) {
		return signature.modPow(e, n);
	}
}
