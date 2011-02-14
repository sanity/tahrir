package tahrir.io.crypto;

public interface SymmetricKey {
	public byte[] encrypt(byte[] toEncrypt);

	public byte[] decrypt(byte[] toDecrypt);
}
