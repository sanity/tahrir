package tahrir.io.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

import tahrir.io.serialization.*;

public class TrHash {
	public byte[] hash;

	protected TrHash() {

	}

	public TrHash(final Object toHash, final int maxSize) throws TrSerializableException {
		final ByteBuffer bb = ByteBuffer.allocate(maxSize);
		TrSerializer.serializeTo(toHash, bb);
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
		if (!(obj instanceof TrHash))
			return false;
		final TrHash other = (TrHash) obj;
		if (!Arrays.equals(hash, other.hash))
			return false;
		return true;
	}

	public BigInteger toBigInteger() {
		// TODO Auto-generated method stub
		return new BigInteger(hash);
	}

}
