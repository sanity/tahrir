package tahrir.util.crypto;

import java.util.Arrays;


public class TrSignature {
	public byte[] signature;

	// for serialization
	public TrSignature() {

	}

	protected TrSignature(final byte[] signature) {
		this.signature = signature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(signature);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TrSignature other = (TrSignature) obj;
		if (!Arrays.equals(signature, other.signature))
			return false;
		return true;
	}
}
