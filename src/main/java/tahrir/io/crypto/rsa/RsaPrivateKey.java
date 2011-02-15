package tahrir.io.crypto.rsa;

import java.math.BigInteger;

import tahrir.io.crypto.*;

public class RsaPrivateKey implements PrivateKey {

	private final BigInteger d;
	private final BigInteger n;
	private final RsaAes256CryptoEngine engine;

	protected RsaPrivateKey(final BigInteger d, final BigInteger n, final RsaAes256CryptoEngine engine) {
		this.d = d;
		this.n = n;
		this.engine = engine;
	}

	public BigInteger decrypt(final BigInteger toDecrypt) {
		return toDecrypt.modPow(d, n);
	}

	public <T> Signature sign(final T toSign) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCryptoAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}
}
