package tahrir.io.crypto;


public interface PublicKey {
	public <T> Encrypted<T> encrypt(T toEncrypt);

	public boolean verify(Signature signed, Object data);
}
