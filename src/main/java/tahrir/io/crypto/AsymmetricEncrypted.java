package tahrir.io.crypto;

import java.math.BigInteger;

public class AsymmetricEncrypted<T> {
	public final BigInteger encryptedSymmetricKey;

	public final SymmetricEncrypted<T> encryptedData;

	public AsymmetricEncrypted(final BigInteger encryptedSymmetricKey, final SymmetricEncrypted<T> encryptedData) {
		this.encryptedSymmetricKey = encryptedSymmetricKey;
		this.encryptedData = encryptedData;
	}

}
