package tahrir.io.crypto;


public interface PrivateKey {
	public <T> T decrypt(AsymmetricEncrypted<T> toEncrypt);

	public <T> Signature sign(T toSign);

	public String getCryptoAlgorithm();
}
