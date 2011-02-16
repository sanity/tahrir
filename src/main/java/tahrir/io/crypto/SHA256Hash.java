package tahrir.io.crypto;

import java.security.*;
import java.util.Arrays;

public class SHA256Hash {
	public final byte[] hash;

	public SHA256Hash(final byte[] toHash) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			hash = digest.digest(toHash);
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

}
