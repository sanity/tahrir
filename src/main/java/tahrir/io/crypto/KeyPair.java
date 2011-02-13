package tahrir.io.crypto;

public class KeyPair {
	public final PublicKey publicKey;

	public final PrivateKey privateKey;

	public KeyPair(final PublicKey publicKey, final PrivateKey privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
}
