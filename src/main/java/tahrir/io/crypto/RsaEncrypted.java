package tahrir.io.crypto;

import java.math.BigInteger;

public class RsaEncrypted {
	public final BigInteger cypherText;

	public RsaEncrypted(final BigInteger cypherText) {
		this.cypherText = cypherText;
	}
}
