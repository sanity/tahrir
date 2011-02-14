package tahrir.io.crypto;

public class AsymmetricKeyPair {
	public final PublicKey publicKey;

	public final PrivateKey privateKey;

	public AsymmetricKeyPair(final PublicKey publicKey, final PrivateKey privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
}
