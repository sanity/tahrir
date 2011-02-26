package tahrir.io.crypto;

import java.math.BigInteger;

public class RsaSignature {
	public BigInteger signature;

	protected RsaSignature() {

	}

	public RsaSignature(final BigInteger signature) {
		this.signature = signature;
	}
}
