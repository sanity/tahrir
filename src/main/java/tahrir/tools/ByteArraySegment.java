package tahrir.tools;

import java.io.*;
import java.net.DatagramPacket;
import java.util.Arrays;

import tahrir.TrConstants;

public final class ByteArraySegment {
	public final byte[] array;
	public final int offset;
	public final int length;

	public static ByteArraySegment from(final DatagramPacket dp) {
		final byte[] array = new byte[dp.getLength()];
		// Create defensive copy of array to ensure immutability
		System.arraycopy(dp.getData(), dp.getOffset(), array, 0, dp.getLength());
		return new ByteArraySegment(array);
	}

	private ByteArraySegment(final byte[] array, final int offset, final int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public ByteArraySegment(final byte[] array) {
		this.array = array;
		offset = 0;
		length = array.length;
	}

	public ByteArrayInputStream toBAIS() {
		return new ByteArrayInputStream(array, offset, length);
	}

	public DataInputStream toDataInputStream() {
		return new DataInputStream(toBAIS());
	}

	public void writeTo(final OutputStream os) throws IOException {
		os.write(array, offset, length);
	}

	public ByteArraySegment subsegment(final int offset, final int length) {
		return new ByteArraySegment(array, this.offset + offset, length);
	}

	public static ByteArraySegmentBuilder builder() {
		return new ByteArraySegmentBuilder();
	}


	public static final class ByteArraySegmentBuilder extends DataOutputStream {

		public ByteArraySegmentBuilder() {
			super(new ByteArrayOutputStream(TrConstants.DEFAULT_BAOS_SIZE));
		}

		public ByteArraySegment build() {
			try {
				flush();
				final ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
				return new ByteArraySegment(baos.toByteArray());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(array);
		result = prime * result + length;
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ByteArraySegment))
			return false;
		final ByteArraySegment other = (ByteArraySegment) obj;
		if (length != other.length)
			return false;
		for (int x = 0; x < length; x++) {
			if (array[offset + x] != other.array[other.offset + x])
				return false;
		}
		return true;
	}
}
