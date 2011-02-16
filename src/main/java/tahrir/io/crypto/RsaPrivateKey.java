package tahrir.io.crypto;

import java.math.BigInteger;

public class RsaPrivateKey {

	public final BigInteger d;
	public final BigInteger n;

	public RsaPrivateKey(final BigInteger d, final BigInteger n) {
		this.d = d;
		this.n = n;

	}

	public BigInteger decrypt(final BigInteger toDecrypt) {
		return toDecrypt.modPow(d, n);
	}

	public BigInteger sign(final BigInteger toSign) {
		return toSign.modPow(d, n);
	}

}
