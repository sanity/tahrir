package tahrir.io.crypto;


public interface PublicKey {
	public <T> AsymmetricEncrypted<T> encrypt(T toEncrypt);

	public boolean verify(Signature signed, Object data);

	public String getCryptoAlgorithm();
}
