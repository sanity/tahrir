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

	public static ByteArraySegment from(final InputStream is, final int maxLength) throws IOException {
		final byte[] ba = new byte[maxLength];
		final int len = is.read(ba);
		assert is.read() == -1;
		return new ByteArraySegment(ba, 0, len);
	}

	private ByteArraySegment(final byte[] array, final int offset, final int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public boolean startsWith(final ByteArraySegment other) {
		if (other.length > length)
			return false;
		for (int x = 0; x < other.length; x++) {
			if (byteAt(x) != other.byteAt(x))
				return false;
		}
		return true;
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

	public ByteArraySegment subsegment(final int offset) {
		return subsegment(offset, Integer.MAX_VALUE);
	}

	public ByteArraySegment subsegment(final int offset, final int length) {
		return new ByteArraySegment(array, this.offset + offset,
				Math.min(length, array.length - (this.offset + offset)));
	}

	public static ByteArraySegmentBuilder builder() {
		return new ByteArraySegmentBuilder();
	}


	public static final class ByteArraySegmentBuilder extends DataOutputStream {

		public void write(final ByteArraySegment seg) {
			try {
				this.write(seg.array, seg.offset, seg.length);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

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

	public final byte byteAt(final int pos) {
		if (pos > length)
			throw new ArrayIndexOutOfBoundsException("byteAt(" + pos + ") but length is " + length);
		return array[offset + pos];
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
			if (byteAt(x) != other.byteAt(x))
				return false;
		}
		return true;
	}
}
