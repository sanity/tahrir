package tahrir.io.crypto;

import java.math.BigInteger;

public class AsymmetricEncrypted<T> {
	public final BigInteger encryptedSymmetricKey;

	public final SymmetricEncrypted<T> encryptedData;

	private final Class<T> type;

	public AsymmetricEncrypted(final BigInteger encryptedSymmetricKey, final SymmetricEncrypted<T> encryptedData,
			final Class<T> type) {
		this.encryptedSymmetricKey = encryptedSymmetricKey;
		this.encryptedData = encryptedData;
		this.type = type;
	}

	public T decrypt(final PrivateKey privateKey) {

	}
}
