package tahrir.io.crypto.rsa;

import java.math.BigInteger;

import tahrir.io.crypto.*;

public class RsaPublicKey implements PublicKey {

	public final BigInteger e;
	public final BigInteger n;
	private final RsaAes256CryptoEngine engine;

	protected RsaPublicKey(final BigInteger e, final BigInteger n, final RsaAes256CryptoEngine engine) {
		this.e = e;
		this.n = n;
		this.engine = engine;
	}

	public <T> AsymmetricEncrypted<T> encrypt(final T toEncrypt) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean verify(final Signature signed, final Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCryptoAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}

}
