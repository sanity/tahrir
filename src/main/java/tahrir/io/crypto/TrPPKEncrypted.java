package tahrir.io.crypto;

import tahrir.tools.ByteArraySegment;

public class TrPPKEncrypted<T> {
	public final byte[] rsaEncryptedAesKey;
	public final ByteArraySegment aesCypherText;

	public TrPPKEncrypted(final byte[] rsaEncryptedAesKey, final ByteArraySegment aesEncrypted) {
		this.rsaEncryptedAesKey = rsaEncryptedAesKey;
		this.aesCypherText = aesEncrypted;
	}
}
