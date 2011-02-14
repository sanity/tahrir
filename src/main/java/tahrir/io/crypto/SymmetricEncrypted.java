package tahrir.io.crypto;

public class SymmetricEncrypted<T> {
	public final byte[] encryptedData;

	public SymmetricEncrypted(final byte[] encryptedData) {
		this.encryptedData = encryptedData;
	}
}
