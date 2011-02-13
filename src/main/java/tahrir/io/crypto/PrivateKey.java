package tahrir.io.crypto;


public interface PrivateKey {
	public <T> T decrypt(Encrypted<T> toEncrypt);

	public <T> Signature sign(T toSign);
}
