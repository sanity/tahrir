package tahrir.io.crypto;

public interface Serializer {
	public byte[] serialize(Object object);

	public <T> T deserialize(byte[] data, Class<T> object);
}
