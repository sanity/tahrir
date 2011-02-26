package tahrir.io.crypto;


public class TrPPKEncrypted<T> {
	public final byte[] rsaEncryptedAesKey;
	public final byte[] aesCypherText;

	public TrPPKEncrypted(final byte[] rsaEncryptedAesKey, final byte[] aesCypherText) {
		this.rsaEncryptedAesKey = rsaEncryptedAesKey;
		this.aesCypherText = aesCypherText;
	}
}
