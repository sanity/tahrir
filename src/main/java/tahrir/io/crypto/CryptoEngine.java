package tahrir.io.crypto;

import tahrir.io.Serializer;

public abstract class CryptoEngine {
	protected final Serializer serializer;

	public CryptoEngine(final Serializer serializer) {
		this.serializer = serializer;
	}

	public abstract AsymmetricKeyPair createKeyPair();

	public abstract SymmetricKey createKey();
}
