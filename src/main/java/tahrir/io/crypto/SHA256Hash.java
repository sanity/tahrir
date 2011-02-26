package tahrir.io.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

import tahrir.io.serialization.*;

public class SHA256Hash {
	public byte[] hash;

	protected SHA256Hash() {

	}

	public SHA256Hash(final Object toHash, final int maxSize) throws TahrirSerializableException {
		final ByteBuffer bb = ByteBuffer.allocate(maxSize);
		TahrirSerializer.serializeTo(toHash, bb);
		bb.flip();
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			digest.update(bb);
			hash = digest.digest();
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SHA256Hash))
			return false;
		final SHA256Hash other = (SHA256Hash) obj;
		if (!Arrays.equals(hash, other.hash))
			return false;
		return true;
	}

	public BigInteger toBigInteger() {
		// TODO Auto-generated method stub
		return new BigInteger(hash);
	}

}
