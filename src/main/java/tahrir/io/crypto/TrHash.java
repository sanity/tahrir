package tahrir.io.crypto;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;

import tahrir.io.serialization.*;

import com.google.common.io.NullOutputStream;

public class TrHash {
	public byte[] hash;

	protected TrHash() {

	}

	public TrHash(final Object toHash, final int maxSize) throws TrSerializableException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			final DataOutputStream digOS = new DataOutputStream(new DigestOutputStream(new NullOutputStream(), digest));
			TrSerializer.serializeTo(toHash, digOS);
			hash = digest.digest();
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
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
